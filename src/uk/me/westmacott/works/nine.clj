(ns uk.me.westmacott.works.nine
  (:require
    [uk.me.westmacott.transcendental :refer :all]
    [uk.me.westmacott.core :as core]
    [uk.me.westmacott.props :as props]
    [uk.me.westmacott.masking :as masking]
    [uk.me.westmacott.colour-sorters :as colour-sorters]
    [uk.me.westmacott.composite :as composite])
  (:import
    [uk.me.westmacott Constants AvailablePointsByTargetColour]
    [java.awt Graphics2D BasicStroke Color Polygon]))


; side-scroller for IG

(def these-colours
  (concat
    (colour-sorters/colours-closest-to
      Color/PINK
      (/ (* 1080 1080 3) Constants/PIXEL_COUNT))
    (sort (colour-sorters/colours-closest-to
            Color/ORANGE
            (/ (* 1080 1080 1) Constants/PIXEL_COUNT)))))

(defn drop-nth [every n coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (concat (take every s) (drop-nth every n (drop (+ every n) s))))))

(defn poly [& xys]
  (reduce (fn [p [x y]]
            (.addPoint p (int x) (int y))
            p)
          (Polygon.)
          (partition 2 xys)))

(defn masking [canvas size width height]
  (masking/with-masking-graphics [^Graphics2D g canvas]
    (masking/set-masking g)
    (.setStroke g (BasicStroke. 5))

    (doseq [x (map #(+ (/ size 2) (* 0.9 %)) (range (- size) (inc (/ size 2)) (/ size 10)))
            y (map #(+ (/ size 2) (* 0.9 %)) (range (- size) size (/ size 10)))]
      (let [xc (- x (* size 2/3))
            yc (- y (* size 1/2))
            theta (if (pos? xc)
                    (taus 1/4)
                    (Math/atan2 yc xc))
            dx (* 15 (Math/sin theta))
            dy (* 15 (Math/cos theta))]
        (when-not (and (< -1 xc)
                       (< -1 yc 1))
          (.drawLine g (+ x dx) (- y dy) (- x dx) (+ y dy)))))

    (.drawLine g (* 3 size) 0 (* 3 size) size)

    (.fillRect g (* size 8/10) (* size 8/20) (* size 2/10) (* size 2/10))

    (.fill g (poly (* size 1/1) (* size 6/20)
                   (* size 1/1) (* size 14/20)
                   (* size 24/20) (* size 10/20)))
    (.fill g (poly (* size 26/20) (* size 0)
                   (* size 34/20) (* size 0)
                   (* size 30/20) (* size 4/20)))
    (.fill g (poly (* size 26/20) (* size 1)
                   (* size 34/20) (* size 1)
                   (* size 30/20) (* size 16/20)))
    (.fill g (poly (* size 60/20) (* size -14/20)
                   (* size 60/20) (* size 34/20)
                   (* size 36/20) (* size 10/20)))
    (.fillRect g (* size 3) 0 size size)

    (masking/set-unmasking g)
    (doseq [x (range 0 (* size 2) 10)
            _ (range 0 (Math/pow x 1.3) 100)]
      (let [xp (+ x (* size 2) (rand 10))
            y1 (rand size)
            y2 (+ y1 (- (rand 30) 15))]
        (.drawLine g xp y1 (+ xp 50) y2)))

    (doseq [x (props/prange 200)
            _ (props/prange ((props/bend (props/straight 1000 1) 1/20) x))
            :when (zero? (rand-int 10))]
      (let [xp (+ (* x size 2) (* size 3) (rand 10))
            yp (rand size)
            r (rand ((props/straight 1 200) x))]
        (if (zero? (rand-int 4))
          (masking/set-masking g)
          (masking/set-unmasking g))
        (.fillArc g (- xp r) (- yp r) (* 2 r) (* 2 r) 0 360)))



    ))

(defn seeding [^AvailablePointsByTargetColour available size]
  (.add available Color/BLACK (int (* size 3/4)) (int (/ size 2))))

(defn canvas-preparer [size width height]
  (let []
    (fn [canvas available]
      (masking canvas size width height)
      (seeding available size)
      available)))

(defn render []
  (let [size 1080
        width (* size 10)
        height size
        colours these-colours
        canvas-prep (canvas-preparer size width height)
        random-seed 1]

    ;(masking/preview-masking width height canvas-prep 1)

    (let [{:keys [mask colours]} (core/render width height (constantly colours) canvas-prep
                                              :random-seed random-seed
                                              :wrap false)]
      (composite/compose colours mask))))


