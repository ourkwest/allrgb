(ns uk.me.westmacott.works.two
  (:require [uk.me.westmacott.core :as core]
            [uk.me.westmacott.colour-sorters :as sorters]
            [uk.me.westmacott.masking :as masking])
  (:import [uk.me.westmacott Constants]
           [java.awt Graphics2D]))




(defn honeycomb [size]
  (fn [canvas availabilities]
    (let [n 10
          hex-radius (/ size n)
          half-size (/ size 2)
          limit (/ (- size hex-radius hex-radius hex-radius) 2)
          [ix iy] [(* (Math/sqrt 3) hex-radius) 0]
          [jx jy] [(* (Math/sqrt 3) hex-radius 0.5) (* 1.5 hex-radius)]
          factor (/ 1 Constants/GOLDEN_RATIO)
          hex-sizes [[(* hex-radius 0.95) (* hex-radius 0.95 factor factor)]
                     [(* hex-radius 0.95) nil]
                     [(* hex-radius 0.95 factor) nil]
                     [(* hex-radius 0.95 factor factor) nil]
                     [nil nil]]]
      (masking/with-masking-graphics [^Graphics2D g canvas]

        (doseq [i (range (- n) n)
                j (range (- n) n)
                :let [x (+ (* i ix) (* j jx))
                      y (+ (* i iy) (* j jy))
                      [hex-size-a hex-size-b] (rand-nth hex-sizes)
                      entrance-a (+ (/ Constants/TAU 12)
                                    (rand-nth (range 0 Constants/TAU (/ Constants/TAU 6))))]
                ;:when (< (+ (* x x) (* y y)) (* limit limit))
                ]

          (when hex-size-a
            (.fill g
                   (-> (core/polygon (+ half-size x) (+ half-size y) hex-size-a 6 0)
                       (core/shape-subtract (core/polygon (+ half-size x) (+ half-size y) (* factor hex-size-a) 6 0))
                       (core/shape-subtract (core/polygon (+ half-size x (* 0.7 hex-size-a (Math/sin entrance-a)))
                                                          (+ half-size y (* 0.7 hex-size-a (Math/cos entrance-a)))
                                                          (/ hex-size-a 4)
                                                          4
                                                          (+ entrance-a (/ Constants/TAU 8))))))
            (when hex-size-b
              (let [entrance-b (+ entrance-a
                                  (rand-nth (range (/ Constants/TAU 6) Constants/TAU (/ Constants/TAU 6))))]
                (.fill g
                       (-> (core/polygon (+ half-size x) (+ half-size y) hex-size-b 6 0)
                           (core/shape-subtract (core/polygon (+ half-size x) (+ half-size y) (* factor hex-size-b) 6 0))
                           (core/shape-subtract (core/polygon (+ half-size x (* 0.7 hex-size-b (Math/sin entrance-b)))
                                                              (+ half-size y (* 0.7 hex-size-b (Math/cos entrance-b)))
                                                              (/ hex-size-b 4)
                                                              4
                                                              (+ entrance-b (/ Constants/TAU 8))))))))
            ))
        )

      (doseq [x (range 0 half-size (int (/ half-size 10)))]
        ;(println x)
        (.add availabilities 0 x 0)
        (.add availabilities 0 0 x))

      ))

  )



(defn render []
  (let [width
        ;1000
        ;#_
            (* Constants/IMAGE_SIZE 1.25)
        height width
        prep-fn (honeycomb width)]

    #_
    (masking/preview-masking width height prep-fn)
    ;#_
    (core/render width height
                 sorters/by-hue
                 prep-fn
                 0)))

