(ns uk.me.westmacott.works.six
  (:require
    [uk.me.westmacott.core :as core]
    [uk.me.westmacott.masking :as masking])
  (:import
    [uk.me.westmacott Constants AvailablePointsByTargetColour]
    [java.awt Graphics2D BasicStroke Color]))


; a big dashed spiral

(def these-colours
  (map int (range Constants/PIXEL_COUNT)))

(defn spiral [width height]
  (for [n (range 0 (* 2 Math/PI 10) 0.01)
        :let [r (* n 100)]]
    [(+ (/ width 2) (* r (Math/sin n)))
     (+ (/ height 2) (* r (Math/cos n)))]))

(defn drop-nth [every n coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (concat (take every s) (drop-nth every n (drop (+ every n) s))))))

(defn masking [canvas width height]
  (masking/with-masking-graphics [^Graphics2D g canvas]
    (masking/set-masking g)
    (.setStroke g (BasicStroke. 10))
    (let [spiral-points (spiral width height)
          r-factor 1.2
          span 104
          gap 5
          section (+ span gap)]
      (doseq [[[x1 y1] [x2 y2]] (drop-nth span gap (partition 2 1 spiral-points))]
        (.drawLine g x1 y1 x2 y2))

      (let [[x y] (first spiral-points)
            radius 15]
        (.fillArc g (- x radius) (- y radius) (* radius 2) (* radius 2) 0 360))

      (doseq [[idx [x y]] (map-indexed vector (take-nth section spiral-points))
              :let [radius (* idx r-factor)]]
        (.fillArc g (- x radius) (- y radius) (* radius 2) (* radius 2) 0 360))

      (doseq [[idx [x y]] (map-indexed vector (take-nth section (drop span spiral-points)))
              :let [radius (* (inc idx) r-factor)]]
        (.fillArc g (- x radius) (- y radius) (* radius 2) (* radius 2) 0 360))

      )))

(defn seeding [^AvailablePointsByTargetColour available width height]
  (.add available Color/BLACK
        (int (+ (/ width 2) 40))
        (int (- (/ height 2) 0))))

(defn canvas-preparer [width height]
  (let []
    (fn [canvas available]
      (masking canvas width height)
      (seeding available width height)
      available)))

(defn render []
  (let [width (int (* Constants/IMAGE_SIZE 1.01))
        height (int (* Constants/IMAGE_SIZE 1.01))
        colours (reverse these-colours)
        canvas-prep (canvas-preparer width height)
        random-seed 1]

    ;(masking/preview-masking width height canvas-prep 1)

    (core/render width height (constantly colours) canvas-prep
                 :random-seed random-seed
                 :wrap :wrap)

    ))


