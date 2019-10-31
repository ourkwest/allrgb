(ns uk.me.westmacott.works.one
  (:require [uk.me.westmacott.core :as core]
            [uk.me.westmacott.colour-sorters :as sorters]
            [uk.me.westmacott.masking :as masking])
  (:import [uk.me.westmacott Constants AvailablePointsByTargetColour]
           [java.awt Graphics2D]))




(defn three-shapes-disjointed [shape-size gap]
  (fn [canvas availabilities]
    (let [width (core/width canvas)
          height (core/height canvas)]
      (masking/with-masking-graphics [^Graphics2D g canvas]

        (.fillArc g
                  (* 2 gap)
                  (* 2 gap)
                  (- shape-size gap gap)
                  (- shape-size gap gap) 0 360)
        ;(.fillRect g (+ gap gap shape-size) gap shape-size shape-size)

        (.fillPolygon g (core/polygon (+ gap gap shape-size (/ shape-size 2))
                                      (+ gap (/ shape-size 2))
                                      (/ shape-size 2)
                                      4
                                      (/ Constants/TAU 8)))

        (.fillPolygon g (core/polygon (+ gap gap gap shape-size shape-size (/ shape-size 2))
                                      (+ gap (* gap 1.1) (/ shape-size 2))
                                      (/ shape-size 2)
                                      3
                                      (/ Constants/TAU 6)))
        )

      (doseq [x (range 0 width (int (/ width 120)))]
        ;(println x)
        (.add availabilities (int (* x 256 256 256 (/ 1 width))) x 0)
        (.add availabilities (int (* x 256 256 256 (/ 1 width))) x (dec height)))

      ))

  )



(defn render []
  (let [shape-size (/ Constants/IMAGE_SIZE 1.3)
        gap-size (/ shape-size 10)
        width (+ (* 4 gap-size) (* 3 shape-size))
        height (+ (* 2 gap-size) shape-size)
        prep-fn (three-shapes-disjointed shape-size gap-size)]

    #_(masking/preview-masking width height prep-fn)
    (core/render width height
                 sorters/by-saturation
                 (three-shapes-disjointed shape-size gap-size))))

