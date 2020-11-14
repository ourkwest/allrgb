(ns uk.me.westmacott.masking
  (:require
    [uk.me.westmacott.core :as core]
    [clojure.java.io :as io])
  (:import
    [java.awt.image BufferedImage]
    [java.awt Color Graphics2D]
    [uk.me.westmacott Constants AvailablePointsByTargetColour]
    [javax.imageio ImageIO]))




(defn set-where [canvas x-y-predicate colour]
  (println "setting mask: " (alength canvas) (alength (aget canvas 0)))
  (doseq [x (range (alength canvas))
          y (range (alength (aget canvas 0)))]
    (when (x-y-predicate x y)
      (aset-int canvas x y colour)))
  (println "set mask: " (alength canvas) (alength (aget canvas 0))))

(def ^:dynamic *preview* nil)

(defn set-masking [g]
  (.setColor g Color/WHITE))

(defn set-unmasking [g]
  (.setColor g Color/BLACK))

(defmacro with-masking-graphics [[graphics-symbol canvas] & drawing-instructions]
  `(let [image# (BufferedImage. (alength ~canvas) (alength (aget ~canvas 0)) BufferedImage/TYPE_BYTE_BINARY)
         ~graphics-symbol (.getGraphics image#)]
     (.setColor ~graphics-symbol Color/WHITE)
     ~@drawing-instructions
     (println "finished masking drawing instructions")
     (if *preview*
       (ImageIO/write image#, "png", (io/file (str "preview-masking-" *preview* ".png")))
       (set-where ~canvas (fn [x# y#] (= (.getRGB image# x# y#) (.getRGB Color/WHITE))) Constants/MASKED))))

(defn preview-masking [width height canvas-preparer & [random-seed]]
  (when random-seed (core/set-random-seed random-seed))
  (binding [*preview* (str "seed-" random-seed)]
    (let [canvas (core/new-unset-2d-int-array width height)]
      (canvas-preparer canvas (AvailablePointsByTargetColour.))))

  #_(let [canvas (core/new-unset-2d-int-array width height)]
    (println "Preparing Canvas...")
    (canvas-preparer canvas (AvailablePointsByTargetColour.))
    (println "Rendering Masking...")
    (let [image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
          graphics (.getGraphics image)
          counter (volatile! {:masked 0 :free 0})]
      (doseq [x (range (int width))
              y (range (int height))]
        (try
          (if (= (aget canvas x y) Constants/MASKED)
            (do (.setColor graphics Color/BLACK)
                (vswap! counter update :masked inc))
            (do (.setColor graphics Color/WHITE)
                (vswap! counter update :free inc)))
          (catch ArrayIndexOutOfBoundsException e
            (println x " / " (core/width canvas) " / " width)
            (println y " / " (core/height canvas) " / " height)
            (.printStackTrace e)
            (throw e)))
        (.drawLine graphics x y x y))
      (let [{:keys [masked free]} @counter]
        (println (/ masked (+ masked free) 0.01) "% masked")
        (println (/ free (+ masked free) 0.01) "% free")
        (println (/ free Constants/PIXEL_COUNT 0.01) "% available"))
      (ImageIO/write image, "png", (io/file "preview-masking.png")))))

(defn- test-masking []

  (let [width 150
        height 100
        canvas (core/new-unset-2d-int-array width height)]

    (with-masking-graphics [^Graphics2D g canvas]
      (.fillRect g 10 10 50 50))

    (let [image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
          graphics (.getGraphics image)]
      (doseq [x (range width)
              y (range height)]
        (if (= (aget canvas x y) Constants/MASKED)
          (.setColor graphics Color/BLACK)
          (.setColor graphics Color/WHITE))
        (.drawLine graphics x y x y))
      (ImageIO/write image, "png", (io/file "test-masking.png")))))
