(ns uk.me.westmacott.works.three
  (:require [uk.me.westmacott.core :as core]
            [uk.me.westmacott.colour-sorters :as sorters]
            [uk.me.westmacott.masking :as masking])
  (:import [uk.me.westmacott Constants AvailablePointsByTargetColour]
           [java.awt Graphics2D Color]))



(defn rand-0 [n]
  (- (rand n) (/ n 2)))

(defn trail [draw-fn g x y size angle]
  (loop [x x
         y y
         current-size size
         angle angle
         dangle (rand-0 0.2)
         colors (cycle [Color/BLACK Color/WHITE])]
    (.setColor g (first colors))
    (draw-fn g x y current-size angle)
    (when (pos? current-size)
      (recur x (- y (/ size 5)) (- current-size 5) (+ angle dangle) dangle (rest colors)))))

(defn tree-circle [n]
  (fn [canvas availabilities]
    (let [width (core/width canvas)
          height (core/height canvas)
          center-x (/ width 2)
          center-y (/ height 3)
          spiral-limit 100
          spiral-step 0.25
          spiral-start 10]
      (masking/with-masking-graphics [^Graphics2D g canvas]

        (->>
          #_(for [_ (repeat n nil)]
              {:sides (rand-nth [3 4 5 6 7])
               :x     (- (rand (* 2 width)) (/ width 2))
               :y     (- (rand (* 2 height)) (/ height 2))
               :size  (+ (/ width 80) (rand (/ width 35)))
               :angle (rand Constants/TAU)})
          (for [i (take-while #(> spiral-limit %)
                              (iterate (fn [x] (float (+ x (/ 5 x)))) spiral-start))]
            {:sides (rand-nth [3 4 5 6 7])
             :x     (+ center-x
                       (* i (/ width 0.85 spiral-limit) (Math/sin i))
                       (rand-0 (* i (/ 250 i))))
             :y     (+ center-y
                       (* i (/ height 1.3 spiral-limit) (Math/cos i))
                       (rand-0 (* i (/ 250 i))))
             :size  (* (+ (* 0.9 i) (* 0.2 (rand i))) (/ width 35 spiral-limit))
             :angle (rand Constants/TAU)})
          (sort-by :y)
          (remove (fn [{:keys [y size]}]
                    (neg? (- y size size size))))
          (remove (fn [{:keys [x y size]}]
                    (let [x-diff (Math/abs (- center-x x))
                          y-diff (- y center-y)]
                      (neg? y-diff #_(+ y-diff (* 0.5 x-diff))))))
          (mapv
            (fn [{:keys [sides x y size angle]}]
              (trail (fn [g x y size angle]
                       (.fillPolygon g (core/polygon x y size sides angle 0.7)))
                     g x y size angle))))

        #_(dotimes [_ n]

          (let [sides (rand-nth [1 3 4 5 6 7])
                x (- (rand (* 2 width)) (/ width 2))
                y (- (rand (* 2 height)) (/ height 2))
                size (* (/ width 10) (rand) (rand))
                angle (rand Constants/TAU)]

            (if (= 1 sides)
              (trail (fn [g x y size _angle]
                      (.fillArc g (- x (/ size 2)) (- y (/ size 2)) size size 0 360))
                     g x y size 0)
              (trail (fn [g x y size angle]
                      (.fillPolygon g (core/polygon x y size sides angle)))
                     g x y size angle)))

          ))

      (.add availabilities (int 0) (int (/ width 2)) (int (/ height 3)))

      ))

  )



(defn render []
  (let [width
        ;1000
        ;#_
            (* Constants/IMAGE_SIZE 1.032)
        height width
        prep-fn (tree-circle 350)
        seed 7]

    #_
    (masking/preview-masking width height prep-fn seed)
    #_
    (doseq [s (range 10)]
      (masking/preview-masking width height prep-fn s))
    ;#_
        (core/render-sorter width height
                     (comp
                       reverse
                       sorters/by-saturation)
                     prep-fn
                     seed
                     true)))

