(ns uk.me.westmacott.works.bathroom2
  (:require
    [uk.me.westmacott.core :as core]
    [uk.me.westmacott.colour-sorters :as sorters]
    [uk.me.westmacott.masking :as masking])
  (:import
    [uk.me.westmacott Constants AvailablePointsByTargetColour]
    [java.awt Graphics2D Color]))


(def tile-size 2362)

(defn prepare-cross [cross-size]
  (fn [canvas ^AvailablePointsByTargetColour availabilities]
    (let [width (core/width canvas)
          height (core/height canvas)
          w25 (* width 2/5)
          h25 (* height 2/5)
          w35 (* width 3/5)
          h35 (* height 3/5)
          cross-big (* tile-size 1/2)
          cross-small (* tile-size 1/3)
          cross-tiny (* tile-size 1/4)]
      (masking/with-masking-graphics [^Graphics2D g canvas]
        (.drawLine g w25 (+ h25 cross-big) w25 (- h25 cross-small))
        (.drawLine g (+ w25 cross-big) h25 (- w25 cross-small) h25)
        (.drawLine g w35 (+ h35 cross-small) w35 (- h35 cross-big))
        (.drawLine g (+ w35 cross-small) h35 (- w35 cross-big) h35)

        (doseq [x (range 1/5 4/5 1/5)
                y (range 1/5 4/5 1/5)]
          (.drawLine g (+ (* x width) cross-tiny) (* y height) (- (* x width) cross-tiny) (* y height))
          (.drawLine g (* x width) (+ (* y height) cross-tiny) (* x width) (- (* y height) cross-tiny))))

      (.add availabilities (Color/GREEN) (int (+ w25 3)) (int (+ h25 3)))
      (.add availabilities (Color/RED) (int (+ w25 3)) (int (- h25 3)))
      (.add availabilities (Color/BLUE) (int (- w25 3)) (int (+ h25 3)))
      (.add availabilities (Color/YELLOW) (int (- w25 3)) (int (- h25 3)))

      (.add availabilities (Color/GREEN) (int (+ w35 3)) (int (+ h35 3)))
      (.add availabilities (Color/RED) (int (+ w35 3)) (int (- h35 3)))
      (.add availabilities (Color/BLUE) (int (- w35 3)) (int (+ h35 3)))
      (.add availabilities (Color/YELLOW) (int (- w35 3)) (int (- h35 3)))

      )))


(defn colours [target proportion every]
  (take-nth every (sorters/colours-closest-to (.getRGB target) proportion)))

(defn repeat-colour [colour proportion]
  (let [colour-count (* 256 256 256 proportion)
        colour (int (if (instance? Color colour)
                      (bit-and (.getRGB ^Color colour) 0xFFFFFF)
                      colour))]
    (repeat colour-count colour)))

(defn render []
  (let [width (* tile-size 5)
        height (* tile-size 5)
        prep-fn (prepare-cross (* tile-size 2/3))
        all-colours (concat (colours Color/GREEN 0.25 1)
                            (colours Color/PINK 0.20 2)
                            (colours (Color. 150 0 255) 0.20 2)
                            (colours Color/RED 0.20 10)
                            (colours Color/BLUE 0.20 10)
                            (colours Color/YELLOW 0.20 10)
                            (repeat-colour Color/WHITE 0.02)
                            (colours Color/BLACK 0.04 2)
                            (repeat-colour Color/BLACK 0.02)
                            )
        ; todo : more coloure (x4?)
        ; shuffle?

        all-colours (concat all-colours all-colours all-colours all-colours)

        ]

    #_(masking/preview-masking width height prep-fn)
    (core/render-opts width height
                      all-colours
                      prep-fn)))

