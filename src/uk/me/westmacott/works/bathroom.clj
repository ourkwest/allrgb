(ns uk.me.westmacott.works.bathroom
  (:require
    [uk.me.westmacott.core :as core]
    [uk.me.westmacott.colour-sorters :as sorters]
    [uk.me.westmacott.masking :as masking]
    [clojure.java.io :as io])
  (:import [uk.me.westmacott Constants AvailablePointsByTargetColour]
           [java.awt Graphics2D Color BasicStroke]
           [java.awt.image BufferedImage]
           [javax.imageio ImageIO]))


(def tile-size 2362)
(def tile-pattern
  [[0 0 0 0 0 0 0 0 0 0 0 0]
   [1 1 1 1 1 1 1 1 1 1 1 1]
   [0 0 0 0 0 0 0 0 0 0 0 0]])
(def repeats 6)

(def width (* tile-size (apply max (map count tile-pattern))))
(def height (* tile-size (count tile-pattern)))

(defn indexed-sin-wave [width repeats theta-offset height v-offset density]
  (let [max-theta (* repeats Constants/TAU)]
    (for [[idx theta] (map-indexed vector (range 0 max-theta density))
          :let [x (* theta (/ width max-theta))
                y (+ v-offset
                     (* height (Math/sin (+ theta theta-offset))))]]
      [idx x y])))

(defn wavy-line [tile-size tile-pattern row colours theta-offset size]
  (fn [canvas ^AvailablePointsByTargetColour availabilities]
    (masking/with-masking-graphics [^Graphics2D g canvas]
      (doseq [[y-idx row] (map-indexed vector tile-pattern)
              [x-idx cell] (map-indexed vector row)
              :when (zero? cell)]
        (.fillRect g
                   (* x-idx tile-size)
                   (* y-idx tile-size)
                   tile-size
                   tile-size)))
    (let [indexed-points (indexed-sin-wave (core/width canvas) 6 theta-offset
                                           (* tile-size size) (* tile-size (+ row 0.5)) 0.1)]
      (doseq [[idx x y] indexed-points]
        (.add availabilities ^int (nth colours idx) (int x) (int y))))))

(defn sharp-wave [tile-size row colours wave-width theta-offset size]
  (fn [canvas ^AvailablePointsByTargetColour availabilities]
    (masking/with-masking-graphics [^Graphics2D g canvas]
      (let [width (core/width canvas)
            height (core/height canvas)
            indexed-points (indexed-sin-wave width 6 theta-offset
                                             (* tile-size size) (* tile-size (+ row 0.5)) 0.1)
            segments (partition 2 1 indexed-points)]
        (.setColor g Color/WHITE)
        (.fillRect g 0 0 width height)
        (.setColor g Color/BLACK)
        (.setStroke g (BasicStroke. wave-width BasicStroke/CAP_ROUND BasicStroke/JOIN_ROUND))
        (let [[idx-a x-a y-a] (ffirst segments)]
          (.add availabilities ^int (nth colours idx-a) (int x-a) (int y-a)))
        (doseq [[[idx-a x-a y-a] [_idx-b x-b y-b]] segments]
          (.drawLine g x-a y-a x-b y-b))))))

(defn floret [g [[_ x-a y-a] [_ x-b y-b]] v-accel]
  (.setStroke g (BasicStroke. 10 BasicStroke/CAP_ROUND BasicStroke/JOIN_ROUND))
  (loop [point1 [x-a y-a]
         point2 [x-a y-a]
         step1 [(/ (- x-b x-a) 10) (/ (- y-b y-a) 10)]
         step2 [(/ (- x-b x-a) 10) (/ (- y-b y-a) 10)]
         limit 1000]
    (when (pos? limit)
      (.drawLine g (first point1) (second point1) (first point2) (second point2))
      (let [point3 (map + point1 step1)
            point4 (map + point2 step2)
            step3 [(- (first step1) 0.01) (-> (+ (second step1) v-accel) (min 5) (max -5))]
            step4 [(+ (first step2) 0.01) (-> (+ (second step2) v-accel) (min 5) (max -5))]]
        (recur point3 point4 step3 step4 (dec limit))))))

(defn florets [theta-offset size]
  (fn [canvas availabilities]
    (masking/with-masking-graphics [^Graphics2D g canvas]
      (let [width (core/width canvas)
            height (core/height canvas)
            indexed-points (indexed-sin-wave width 6 theta-offset
                                             (* tile-size size) (* tile-size (+ 1 0.5)) 0.1)
            segments (partition 2 1 indexed-points)]
        (.setColor g Color/WHITE)
        (.fillRect g 0 0 width height)
        (.setColor g Color/BLACK)

        (let [[_ x-a y-a] (ffirst segments)]
          (.add availabilities 8421504 (int x-a) (int y-a)))

        (floret g (nth segments 10) 0.01)
        (floret g (nth segments 50) -0.01)
        (floret g (nth segments 100) 0.01)
        ;(floret2 g (nth segments 100) 0.01)
        ))))

(def green-theta-offset 0)
(def pink-theta-offset (* Constants/TAU 1/3))
(def dark-theta-offset (* Constants/TAU 2/3))

(defn render-green []
  (let [greens (sorters/colours-closest-to (.getRGB (Color. 0 255 0 0)) 0.05)
        shuffled-greens (shuffle greens)
        wavy-prep (wavy-line tile-size tile-pattern 1 shuffled-greens green-theta-offset 0.4)
        sharp-prep (sharp-wave tile-size 1 greens (/ tile-size 10) green-theta-offset 0.4)]
    (core/render width height shuffled-greens wavy-prep 0 true)
    (core/render width height greens sharp-prep 0 true)))

(defn render-pink []
  (let [pinks (take-nth 2 (sorters/colours-closest-to (.getRGB (Color. 255 0 255 0)) 0.06))
        shuffled-pinks (shuffle pinks)
        wavy-prep (wavy-line tile-size tile-pattern 1 shuffled-pinks pink-theta-offset 0.1)
        sharp-prep (sharp-wave tile-size 1 pinks (/ tile-size 20) pink-theta-offset 0.1)]
    (core/render width height shuffled-pinks wavy-prep 0 true)
    (core/render width height pinks sharp-prep 0 true)))

(defn render-dark-green []
  (let [colours (sorters/colours-closest-to (.getRGB (Color. 0 100 0 0)) 0.05) ;0.04?
        shuffled-colours (shuffle colours)
        wavy-prep (wavy-line tile-size tile-pattern 1 shuffled-colours dark-theta-offset 0.2)
        sharp-prep (sharp-wave tile-size 1 colours (/ tile-size 10) dark-theta-offset 0.2)]
    (core/render width height shuffled-colours wavy-prep 0 true)
    (core/render width height colours sharp-prep 0 true)))

(defn render-florets []
  (let [greens (sorters/colours-closest-to (.getRGB (Color. 50 255 0 0)) 0.05)
        pinks (take-nth 2 (sorters/colours-closest-to (.getRGB (Color. 255 0 200 0)) 0.04))
        purples (take-nth 2 (sorters/colours-closest-to (.getRGB (Color. 150 0 255 0)) 0.02))
        colours (concat (filter even? greens) (shuffle (concat (filter odd? greens) pinks purples)))
        floret-prep (florets green-theta-offset 0.4)
        ]
    (masking/preview-masking width height floret-prep 0)
    #_(core/render width height colours floret-prep 0 true)
    ))


(defn render-mural []
  (let [image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (.getGraphics image)
        layers (->> [{:no 129 :theta-offset green-theta-offset :z-shift 0}
                     {:no 130 :theta-offset green-theta-offset :z-shift 10}
                     {:no 141 :theta-offset pink-theta-offset :z-shift 0}
                     {:no 142 :theta-offset pink-theta-offset :z-shift 10}
                     {:no 143 :theta-offset dark-theta-offset :z-shift 0}
                     {:no 144 :theta-offset dark-theta-offset :z-shift 10}]
                    (map (fn [x]
                           (assoc x :image (ImageIO/read (io/file "clj-renders" (str "Render_" (:no x) "-final.png")))))))]
    (doseq [x (range width)]
      (let [theta (-> x
                      (/ width)
                      (* repeats Constants/TAU))
            sorted-layers (sort-by (fn [layer]
                                     (+ (:z-shift layer) (* 100 (Math/cos (+ theta (:theta-offset layer))))))
                                   layers)]
        (doseq [layer sorted-layers]
          (.drawImage g (:image layer)
                      x 0 (inc x) height
                      x 0 (inc x) height
                      nil)
          )

        )
      )
    (ImageIO/write image "png" (io/file "layered.png"))
    ))

#_(defn render-2 []
  (let [purples (sorters/colours-closest-to (.getRGB (Color. 255 0 255 0)) 0.1)
        prep-canvas (wavy-line tile-size tile-pattern 1 purples 0.4)
        prep-canvas (sharp-wave tile-size 1 purples (/ tile-size 10) (/ Constants/TAU 2))
        ]

    #_(println (take 10 purples))

    #_(masking/preview-masking width height prep-canvas)
    (core/render width height purples
                 prep-canvas 0 true)))

(defn render-alpha [theta-offset-proportion]
  (let [image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (.getGraphics image)]

    (.setColor g Color/BLACK)
    (.fillRect g 0 0 width height)

    (doseq [[idx x y] (indexed-sin-wave width 6 (* theta-offset-proportion Constants/TAU) 127.5 127.5 0.0061)]
      (.setColor g (Color. (int y) (int y) (int y)))
      (.drawLine g x 0 x height))

    (ImageIO/write image "png" (io/file (str "alpha-wave-" theta-offset-proportion ".png")))

    ))

(defn render-alphas []
  (render-alpha 0)
  (render-alpha 0.25)
  (render-alpha 0.33)
  (render-alpha 0.5)
  (render-alpha 0.67)
  (render-alpha 0.75))