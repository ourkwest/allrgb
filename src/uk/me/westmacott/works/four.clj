(ns uk.me.westmacott.works.four
  (:require [uk.me.westmacott.core :as core]
            [uk.me.westmacott.colour-sorters :as colour-sorters]
            [uk.me.westmacott.masking :as masking])
  (:import [uk.me.westmacott Constants AvailablePointsByTargetColour ColourSeries]
           [java.awt Graphics2D Color BasicStroke]))



(defn in-bounds? [x y width height]
  (and (< -1 x width)
       (< -1 y height)))

(defn tree-sections
  ([width height] (tree-sections width height {:x (/ width 2) :y height :r 0 :scale 1}))
  ([width height {:keys [x y r scale]}]
   (if (> scale 0.1)
     (let [scale-factor 0.7
           spread-factor 0.2
           xa x
           ya y
           xb (+ x (* height 0.25 scale (Math/sin r)))
           yb (- y (* height 0.25 scale (Math/cos r)))
           xc (+ x (* height 0.26 scale (Math/sin r)))
           yc (- y (* height 0.26 scale (Math/cos r)))]
       (-> [{:xa xa :ya ya :xb xc :yb yc :scale scale}]
           (into (tree-sections width height {:x xb :y yb :r (+ r spread-factor (rand 0.3)) :scale (* scale scale-factor)}))
           (into (tree-sections width height {:x xb :y yb :r (- r spread-factor (rand 0.3)) :scale (* scale scale-factor)}))))
     [])))

(defn seed-tree [available width height tree-bits]
  (doseq [{:keys [xa ya xb yb scale]} tree-bits]
    (let [props (range 0 1 (/ 1 (* 100 scale)))]
      (when (not= (count props)
                  (->> (for [prop props]
                         (let [inv (- 1 prop)
                               px (+ (* xa prop) (* xb inv))
                               py (+ (* ya prop) (* yb inv))]
                           (if (in-bounds? px py width height)
                             (do (.add available
                                       (Color. ^Integer (rand-int 256) ^Integer (rand-int 256) ^Integer (rand-int 256))
                                       (int px) (int py))
                                 1)
                             0)))
                       (reduce +)))
        (println "Clipping!!!")))))

(defn seed-tree-2 [available width height]
  (.add available Color/GREEN (int (/ width 2)) (int (dec height))))

(defn mask-inverse-tree [canvas width height tree-bits]
  (masking/with-masking-graphics [^Graphics2D g canvas]

    (masking/set-masking g)
    (.fillRect g 0 0 width height)

    (masking/set-unmasking g)
    (doseq [{:keys [xa ya xb yb scale]} tree-bits]
      (.setStroke g (BasicStroke. (int (* scale 150)) BasicStroke/CAP_ROUND BasicStroke/JOIN_ROUND))
      (.drawLine g xa ya xb yb))))

(defn mask-tree-edges [canvas width height tree-bits]
  (masking/with-masking-graphics [^Graphics2D g canvas]

    (masking/set-masking g)
    (doseq [{:keys [xa ya xb yb scale]} tree-bits]
      (.setStroke g (BasicStroke. (int (* scale 150)) BasicStroke/CAP_ROUND BasicStroke/JOIN_ROUND))
      (.drawLine g xa ya xb yb))

    (masking/set-unmasking g)
    (doseq [{:keys [xa ya xb yb scale]} tree-bits]
      (.setStroke g (BasicStroke. (int (* scale 140)) BasicStroke/CAP_ROUND BasicStroke/JOIN_ROUND))
      (.drawLine g xa ya xb yb))))

(defn canvas-preparer [width height]
  (let [tree-bits (tree-sections width height)]
    (fn [canvas available]
      (mask-tree-edges canvas width height tree-bits)
      (seed-tree-2 available width height)
      available)))

(defn preview-mask []
  (let [width (int (* Constants/IMAGE_SIZE 1))
        height (int (* Constants/IMAGE_SIZE 1))]
    (masking/preview-masking width height (canvas-preparer width height) 0)))

(defn greens-first [colours]
  (sort-by #(Math/abs (- (ColourSeries/getHue %) (int 120))) colours))

(defn first-third [colours]
  (take (/ Constants/PIXEL_COUNT 3.2) colours))

;(def greenest-third (first-third (greens-first colour-sorters/all-colours)))

(def brown-then-green
  (concat (colour-sorters/colours-closest-to (.getRGB (Color. 150 75 0)) 1/20)
          ;(colour-sorters/colours-closest-to (.getRGB (Color. 0 255 0)) 1/5)
          )
  )

(defn render []
  (let [width (int (* Constants/IMAGE_SIZE 1.5))
        height (int (* Constants/IMAGE_SIZE 1.25))]
    (core/render-opts width height brown-then-green (canvas-preparer width height) 0)))
