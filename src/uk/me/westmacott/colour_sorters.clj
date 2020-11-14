(ns uk.me.westmacott.colour-sorters
  (:import [uk.me.westmacott ColourSeries Constants]
           [java.awt Color]))


(def all-colours
  (map int (range Constants/PIXEL_COUNT)))

(defn by-hue [ints]
  (sort-by (fn [i] (ColourSeries/getHue i)) ints))

(defn by-saturation [ints]
  (sort-by (fn [i] (ColourSeries/getSaturation i)) ints))

(defn by-brightness [ints]
  (sort-by (fn [i] (ColourSeries/getBrightness i)) ints))

(defn rgb-distance-squared [colour-a colour-b]
  (let [ra (bit-and 0xFF (bit-shift-right colour-a 16))
        ga (bit-and 0xFF (bit-shift-right colour-a 8))
        ba (bit-and 0xFF colour-a)
        rb (bit-and 0xFF (bit-shift-right colour-b 16))
        gb (bit-and 0xFF (bit-shift-right colour-b 8))
        bb (bit-and 0xFF colour-b)
        rd (- ra rb)
        gd (- ga gb)
        bd (- ba bb)]
    (+ (* rd rd) (* gd gd) (* bd bd))))

(defn offsetter [proportion]
  (fn [ints]
    (let [n (count ints)
          offset (int (* n proportion))]
      (take n (drop offset (cycle ints))))))

; this was very slow
(defn colours-closest-to-2
  ([target-colour]
   (colours-closest-to-2 target-colour 1))
  ([target-colour proportion]
   (let [colour-count (* Constants/PIXEL_COUNT proportion)]
     (take colour-count (sort-by #(rgb-distance-squared target-colour %) all-colours)))))

(defn split-colour [int-colour]
  (let [r (bit-and 0xFF (bit-shift-right int-colour 16))
        g (bit-and 0xFF (bit-shift-right int-colour 8))
        b (bit-and 0xFF int-colour)]
    [r g b]))

(defn in-bounds? [[r g b]]
  (and (< -1 r 256)
       (< -1 g 256)
       (< -1 b 256)))

(defn combine-colour [[r g b]]
  (int (bit-or (bit-shift-left r 16)
               (bit-shift-left g 8)
               b)))

(defn colours-closest-to
  ([target-colour]
   (colours-closest-to target-colour 1))
  ([target-colour proportion]
   (println "Finding closest colours...")
   (let [colour-count (* Constants/PIXEL_COUNT proportion)
         target-colour (if (instance? Color target-colour)
                         (bit-and (.getRGB ^Color target-colour) 0xFFFFFF)
                         target-colour)
         r (bit-and 0xFF (bit-shift-right target-colour 16))
         g (bit-and 0xFF (bit-shift-right target-colour 8))
         b (bit-and 0xFF target-colour)
         colours
         (->> (for [shell (range 1 256)]
                (let [corners (for [cr [(+ r shell) (- r shell)]
                                    cg [(+ g shell) (- g shell)]
                                    cb [(+ b shell) (- b shell)]]
                                [cr cg cb])
                      edges (apply concat (for [i (range (inc (- shell)) shell)]
                                            [[(- r shell) (- g shell) (+ b i)]
                                             [(- r shell) (+ g shell) (+ b i)]
                                             [(+ r shell) (- g shell) (+ b i)]
                                             [(+ r shell) (+ g shell) (+ b i)]
                                             [(- r shell) (+ g i) (- b shell)]
                                             [(- r shell) (+ g i) (+ b shell)]
                                             [(+ r shell) (+ g i) (- b shell)]
                                             [(+ r shell) (+ g i) (+ b shell)]
                                             [(+ r i) (- g shell) (- b shell)]
                                             [(+ r i) (- g shell) (+ b shell)]
                                             [(+ r i) (+ g shell) (- b shell)]
                                             [(+ r i) (+ g shell) (+ b shell)]]
                                            ))
                      faces (apply concat (for [i (range (inc (- shell)) shell)
                                                j (range (inc (- shell)) shell)]
                                            [[(- r shell) (+ g i) (+ b j)]
                                             [(+ r shell) (+ g i) (+ b j)]
                                             [(+ r i) (- g shell) (+ b j)]
                                             [(+ r i) (+ g shell) (+ b j)]
                                             [(+ r i) (+ g j) (- b shell)]
                                             [(+ r i) (+ g j) (+ b shell)]]))]
                  (concat faces edges corners)))
              (apply concat)
              (filter in-bounds?)
              (map combine-colour)
              (cons target-colour)
              (take colour-count))]
     (doall colours)
     (println "Found closest colours.")
     colours)))
