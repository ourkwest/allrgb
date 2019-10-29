(ns uk.me.westmacott.core
  (:import [uk.me.westmacott Constants AvailablePointsByTargetColour ImageSpitter MountainsLettuceLightningSpace ColourSeries]
           [java.awt Color Point]
           [java.util Arrays]))


(defn new-unset-2d-int-array [width height]
  (let [array-2d (make-array Integer/TYPE width height)]
    (doseq [array-1d array-2d]
      (Arrays/fill ^"[I" array-1d Constants/UNSET))
    array-2d))

(defn render
  "Takes:
    - width of canvas
    - height or canvas
    - a function to sort colours represented as integers
    - a function taking a canvas and an availabilites object
        - canvas is a 2D int array, and can be drawn upon with any integer colour,
            including uk.me.westmacott.Constants/UNSET and uk.me.westmacott.Constants/MASKED
        - availabilities is an AvailablePointsByTargetColour
   Produces:
    a rendered image in a local `clj-renders` directory"
  [width height colour-sorter canvas-preparer]
  (let [canvas (new-unset-2d-int-array width height)
        colours (colour-sorter (map int (range Constants/PIXEL_COUNT)))
        available (AvailablePointsByTargetColour.)
        spitter (ImageSpitter. "clj-renders")]

    (canvas-preparer canvas available)

    (MountainsLettuceLightningSpace/render canvas colours available spitter)))

(defn seed-2-corners [available width height]
  (doto available
    (.add Color/GRAY 1 1)
    (.add Color/GRAY (- width 1) (- height 1))))

(defn seed-random-points [available width height n]
  (dotimes [_ n]
    (.add available
          (Color. ^Integer (rand-int 256) ^Integer (rand-int 256) ^Integer (rand-int 256))
          (rand-int width) (rand-int height))))

(comment
  (let [width (int (* Constants/IMAGE_SIZE 1.05))
        height (int (* Constants/IMAGE_SIZE 1))]
    (render width
            height
            #(sort-by (fn [i] (ColourSeries/getHue i)) %)
            (fn [_canvas available]
              (seed-random-points available width height 10)
              available))))
