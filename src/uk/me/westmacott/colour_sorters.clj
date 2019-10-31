(ns uk.me.westmacott.colour-sorters
  (:import [uk.me.westmacott ColourSeries]))


(defn by-hue [ints]
  (sort-by (fn [i] (ColourSeries/getHue i)) ints))

(defn by-saturation [ints]
  (sort-by (fn [i] (ColourSeries/getSaturation i)) ints))

(defn by-brightness [ints]
  (sort-by (fn [i] (ColourSeries/getBrightness i)) ints))


(defn offsetter [proportion]
  (fn [ints]
    (let [n (count ints)
          offset (int (* n proportion))]
      (take n (drop offset (cycle ints))))))
