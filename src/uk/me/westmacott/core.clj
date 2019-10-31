(ns uk.me.westmacott.core
  (:import [uk.me.westmacott Constants AvailablePointsByTargetColour ImageSpitter MountainsLettuceLightningSpace ColourSeries]
           [java.awt Color Point Polygon]
           [java.util Arrays Random]
           [java.awt.geom Area]
           [java.lang Math$RandomNumberGeneratorHolder]))


(defn new-unset-2d-int-array [width height]
  (let [array-2d (make-array Integer/TYPE width height)]
    (doseq [array-1d array-2d]
      (Arrays/fill ^"[I" array-1d Constants/UNSET))
    array-2d))

(defn width [canvas] (alength canvas))
(defn height [canvas] (alength (aget canvas 0)))

(defn polygon [x y r n angle]
  (let [angles (->> (range 0 Constants/TAU (/ Constants/TAU n))
                    (map (partial + angle)))
        xs (map #(-> % Math/sin (* r) (+ x)) angles)
        ys (map #(-> % Math/cos (* r) (+ y)) angles)]
    (Polygon. (int-array xs) (int-array ys) (count xs))))

(defn shape-subtract [shape-a shape-b]
  (doto (Area. shape-a)
    (.subtract (Area. shape-b))))

(defn set-random-seed [seed]
  (let [field (.getDeclaredField Math$RandomNumberGeneratorHolder "randomNumberGenerator")]
    (.setAccessible field true)
    (.setSeed ^Random (.get field nil) seed)))

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
  [width height colour-sorter canvas-preparer & [random-seed]]
  (let [canvas (new-unset-2d-int-array width height)
        colours (colour-sorter (map int (range Constants/PIXEL_COUNT)))
        available (AvailablePointsByTargetColour.)
        spitter (ImageSpitter. "clj-renders")]

    (when random-seed (set-random-seed random-seed))
    (canvas-preparer canvas available)
    (.spitMask spitter canvas "mask" Color/BLACK)

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
              available)
            0)))
