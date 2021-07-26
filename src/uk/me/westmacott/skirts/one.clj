(ns uk.me.westmacott.skirts.one
  (:require [uk.me.westmacott.core :as core]
            [uk.me.westmacott.colour-sorters :as colour-sorters]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [uk.me.westmacott.masking :as masking])
  (:import [uk.me.westmacott Constants ImageSpitter AvailablePointsByTargetColour]
           [java.awt Color Graphics2D Image BasicStroke]
           [javax.imageio ImageIO]
           [java.io File]
           [java.awt.image BufferedImage]
           [java.time Instant]))

; colour at least to bleed lines
; no guide marks inside the cutting lines (can be on the cutting lines)

(def fabric-width-cm 144)
(def seam-allowance-cm 1.5)
(def skirt-length-cm 53)

(def waist-circumference-cm 74)
(def waist-sew-height-cm 5)
(def waist-cut-height-cm 8)
(def waist-bleed-height-cm 14)

(def pattern-width-cm fabric-width-cm)
(def pattern-height-cm (+ fabric-width-cm waist-bleed-height-cm))

(def inner-sew-circumference-cm waist-circumference-cm)
(def inner-sew-radius-cm (/ waist-circumference-cm Constants/TAU))
(def inner-cut-radius-cm (- inner-sew-radius-cm seam-allowance-cm))
(def inner-cut-diameter-cm (* inner-cut-radius-cm 2))

(def outer-sew-radius-cm (+ inner-sew-radius-cm skirt-length-cm))
(def outer-cut-radius-cm (+ outer-sew-radius-cm seam-allowance-cm))
(def outer-bleed-radius-cm (+ outer-cut-radius-cm seam-allowance-cm))
(def outer-bleed-diameter-cm (* 2 outer-bleed-radius-cm))

(def outer-bleed-diameter-px 5000)
(def pixels-per-cm (/ outer-bleed-diameter-px outer-bleed-diameter-cm))
(defn px [cm]
  (* cm pixels-per-cm))
; could be bigger, smaller than 4000 might hit quality limits on the printing.
(def image-width (px fabric-width-cm))
(def initial-growth-height-factor 1)

(println (str "Print on " pattern-width-cm " x " pattern-height-cm "\n Pixels / cm: " (float pixels-per-cm)))

(defn skirt-spitter []
  (ImageSpitter/onceEvery (int 1000000) (ImageSpitter/forDirectory "skirt-renders")))

(defn draw-circle [g x y r]
  (.drawArc g (- x r) (- y r) (* r 2) (* r 2) 0 360))

(defn canvas-preparer [width height]
  ; TODO: mask out some white areas?
  (let [x (int (* width 1/2))
        y (int (* height 1/2))]
    (fn [canvas available]

      (masking/with-masking-graphics [^Graphics2D g canvas]
        (masking/set-masking g)
        (.setStroke g (BasicStroke. 2)) ; so they don't jump through!
        (draw-circle g x y (/ outer-bleed-diameter-px 2)))

      (.add available Color/BLACK x y)
      available)))

(defn make-skirt-file [^File circle-render-file random-seed]
  (let [circle (ImageIO/read circle-render-file)
        waist-image-height (px waist-bleed-height-cm)
        waist-seed-height (int (px (/ (- waist-bleed-height-cm
                                         waist-sew-height-cm)
                                      2)))
        waist-image-width (px (+ waist-circumference-cm (* 4 seam-allowance-cm)))
        pattern (BufferedImage. image-width (+ image-width waist-image-height) BufferedImage/TYPE_INT_ARGB)
        pattern-g ^Graphics2D (.getGraphics pattern)
        ;skirt (BufferedImage. image-width image-width BufferedImage/TYPE_INT_ARGB)
        ;skirt-g ^Graphics2D (.getGraphics skirt)
        pattern-file (io/file "skirt-renders" (str "pattern-" (-> (Instant/now)
                                                                  str
                                                                  (string/replace #"\W" "")
                                                                  (subs 0 15)) ".png"))
        x (/ image-width 2)
        y x]

    ;(.drawImage pattern-g circle -30 (- -695 (/ image-width 2)) nil)
    (.drawImage pattern-g circle 0 (int (/ (- (* image-width initial-growth-height-factor)
                                              image-width)
                                           2)) nil)

    (let [waist-px (px waist-circumference-cm)
          inner-sew-radius-px (px inner-sew-radius-cm)
          waist-seed-points (for [theta (range 0 Constants/TAU (/ Constants/TAU waist-px))]
                              [(+ x (* inner-sew-radius-px (Math/cos theta)))
                               (+ y (* inner-sew-radius-px (Math/sin theta)))])
          waist-seed-offset 1/4
          seed-colours (colour-sorters/offset-by
                         (mapv (fn [[x y]]
                               (Color. (.getRGB pattern x y)))
                             waist-seed-points)
                         waist-seed-offset)
          source-points (for [x-in-circle (range (- inner-sew-radius-px) inner-sew-radius-px)
                              y-in-circle (range (- inner-sew-radius-px) inner-sew-radius-px)
                              :when (> (* inner-sew-radius-px inner-sew-radius-px)
                                       (+ (* x-in-circle x-in-circle)
                                          (* y-in-circle y-in-circle)))]
                          [(+ x x-in-circle) (+ y y-in-circle)])
          source-colours (->> (for [[x y] source-points]
                                (.getRGB pattern x y))
                              (map colour-sorters/safe))

          waist-file (:colours (core/render
                                 waist-image-width
                                 waist-image-height
                                 #(apply concat (repeat 3 (colour-sorters/shuffled source-colours)))
                                 (fn [_canvas ^AvailablePointsByTargetColour available]
                                   (doseq [i (range waist-px)]
                                     (.add available
                                           ^Color (nth seed-colours i)
                                           (int (+ i (px (* 2 seam-allowance-cm))))
                                           waist-seed-height))
                                   available)
                                 :spitter (skirt-spitter)
                                 :random-seed random-seed))
          waist (ImageIO/read waist-file)
          five-cm-as-px (px 5)]

      (println "waist-file" waist-file)
      waist-seed-height

      (let [waist-piece-left-margin 500
            waist-piece-match-marker 50]
        (.setColor pattern-g Color/BLACK)
        (.drawLine pattern-g
                   (- waist-piece-left-margin waist-piece-match-marker) (+ image-width waist-seed-height)
                   (+ waist-piece-left-margin waist-image-width waist-piece-match-marker) (+ image-width waist-seed-height))
        (.setColor pattern-g Color/BLACK)
        (.drawImage ^Graphics2D pattern-g ^Image waist (int waist-piece-left-margin) (int image-width) nil))

      (draw-circle pattern-g x y (px inner-cut-radius-cm))
      (draw-circle pattern-g x y (px outer-cut-radius-cm))
      #_(.drawArc skirt-g
                  (- x (px inner-cut-radius-cm)) (- y (px inner-cut-radius-cm))
                  (px inner-cut-diameter-cm) (px inner-cut-diameter-cm) 0 360)
      ;(.drawArc skirt-g (- x 50) (- y 50) 100 100 0 360)
      (draw-circle pattern-g x y five-cm-as-px)
      (.drawLine pattern-g x (- y five-cm-as-px) x (+ y five-cm-as-px))
      (.drawLine pattern-g (- x five-cm-as-px) y (+ x five-cm-as-px) y)

      #_(doseq [[x y] waist-seed-points]
        (.setColor pattern-g Color/GREEN)
        (.drawLine pattern-g x y x y))

      #_(doseq [[colour i] (map vector seed-colours (range))]
        (.setColor pattern-g colour)
        (.drawLine pattern-g (+ 100 i) 3 (+ 100 i) 3))

      (ImageIO/write pattern "png" pattern-file)

      pattern-file)))

(defn render [colour-offset]
  (let [width image-width
        height (* initial-growth-height-factor image-width)
        colour-fn #(reverse (colour-sorters/by-hue colour-sorters/all-colours))
        ;colours #(colour-sorters/shuffled colour-sorters/all-colours)
        ;colours #(colour-sorters/colours-closest-to Color/CYAN)
        ;colours #(interleave (colour-sorters/colours-closest-to Color/RED 1/4)
        ;                     (colour-sorters/colours-closest-to Color/YELLOW 1/4)
        ;                     (colour-sorters/colours-closest-to Color/GREEN 1/4)
        ;                     (colour-sorters/colours-closest-to Color/BLUE 1/4))
        ;colour-fn #(colour-sorters/colours-closest-to Color/CYAN)
        colour-fn #(colour-sorters/offset-by (colour-sorters/by-hue colour-sorters/all-colours) colour-offset)
        canvas-prep (canvas-preparer width height)
        random-seed 1 ; there's no randomness in the prepare function

        #_(masking/preview-masking width height canvas-prep 1)
        circle-render-file (:colours (core/render width height colour-fn canvas-prep
                                                  :spitter (skirt-spitter)
                                                  :random-seed random-seed))
        ;circle-render-file (io/file "/home/peter/Downloads/Render_013-final.png")

        skirt-file (make-skirt-file circle-render-file random-seed)
        ]

    (println (str circle-render-file))
    (println (str skirt-file))

    ; load circle
    ; draw cutting lines
    ; read sewing line colours and use as seed for waistband

    ))

(comment
 (doseq [offset (range 0 1 1/8)]
   (render offset))
 )
