(ns uk.me.westmacott.works.five
  (:require
    [uk.me.westmacott.core :as core]
    [uk.me.westmacott.masking :as masking])
  (:import
    [uk.me.westmacott Constants]
    [java.awt Graphics2D BasicStroke Color]))


; something with beziers?

(def these-colours
  (map int (range Constants/PIXEL_COUNT)))

(let [smoothness 1/4
      inset 1/10]
  (defn make-curve-data [width height]
    ; one big long curve that crosses itself?
    ; start with a circle and add loops?
    ; random walk?
    (repeatedly
      10
      (fn []
        {:x  (+ (* width inset)
                (rand-int (* width (- 1 inset inset))))
         :y  (+ (* height inset)
                (rand-int (* height (- 1 inset inset))))
         :dx (- (rand-int (* width smoothness))
                (/ (* width smoothness) 2))
         :dy (- (rand-int (* height smoothness))
                (/ (* height smoothness) 2))}))))

(defn lerp [a b p]
  (+ (* (- 1.0 p) a) (* p b)))

(defn lerp-point [[ax ay] [bx by] p]
  [(lerp ax bx p) (lerp ay by p)])

(def p-range (range 0 1.001 1/100))

(defn draw-bezier [g
                   {x1 :x y1 :y dx1 :dx dy1 :dy}
                   {x2 :x y2 :y dx2 :dx dy2 :dy}
                   available
                   v-colours]
  (let [a [x1 y1]
        b [(+ x1 dx1) (+ y1 dy1)]
        c [(- x2 dx2) (- y2 dy2)]
        d [x2 y2]
        points (for [p p-range]
                 (let [ab (lerp-point a b p)
                       bc (lerp-point b c p)
                       cd (lerp-point c d p)
                       abc (lerp-point ab bc p)
                       bcd (lerp-point bc cd p)
                       abcd (lerp-point abc bcd p)]
                   abcd))
        point-pairs (partition 2 1 points)]
    (doseq [[[x1 y1] [x2 y2]] point-pairs]
      (let [dx (- x1 x2)
            dy (- y1 y2)
            c (first (vswap! v-colours rest))]
        (.add available c (int (+ x1 dx)) (int (- y1 dy)))
        (.add available c (int (- x1 dx)) (int (+ y1 dy))))
      (.drawLine g x1 y1 x2 y2))
    point-pairs))

(defn masking [canvas available width height colours]
  (let [curve-data (make-curve-data width height)
        curve-pairs (->> curve-data
                         (cycle)
                         (partition 2 1)
                         (take (count curve-data)))
        v-colours (volatile! colours)]
    (masking/with-masking-graphics [^Graphics2D g canvas]
      (masking/set-masking g)

      (.setStroke g (BasicStroke. 10))
      (doseq [[p1 p2] curve-pairs]
        (draw-bezier g p1 p2 available v-colours))

      )))

(defn seeding [available width height]
  )

(defn canvas-preparer [width height colours]
  (let []
    (fn [canvas available]
      (masking canvas available width height colours)
      ;(seeding available width height)
      available)))

(defn render []
  (let [width (int (* Constants/IMAGE_SIZE 1.25))
        height (int (* Constants/IMAGE_SIZE 1.25))
        colours these-colours
        canvas-prep (canvas-preparer width height colours)
        random-seed 1]

    #_(masking/preview-masking width height canvas-prep 1)

    (core/render-opts width height colours
                      canvas-prep random-seed :wrap)))


