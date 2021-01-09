(ns uk.me.westmacott.composite
  (:require [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [java.io File]
           [javax.imageio ImageIO]
           [java.awt.image BufferedImage]))


(defn compose [^File file1 ^File file2 ^File file3]
  (let [image2 (ImageIO/read file2)
        g (.getGraphics image2)]
    (.drawImage g (ImageIO/read file1) 0 0 nil)
    (ImageIO/write image2 "png" file3)))

(defn- running-totals
  ([ns] (running-totals ns 0))
  ([ns running-total]
   (when (seq ns)
     (let [running-total' (+ (first ns) running-total)]
       (cons running-total' (running-totals (rest ns) running-total'))))))

(defn split [^File file xs ys]
  (let [source (ImageIO/read file)
        child-file-fn (let [parent (.getParentFile file)
                            [fname ext] (string/split (.getName file) #"\.")]
                        (fn [n]
                          (io/file parent (str fname "-split-" n "." ext))))]
    (doseq [[xi [x1 x2]] (map-indexed vector (partition 2 1 xs))
            [yi [y1 y2]] (map-indexed vector (partition 2 1 ys))]
      (let [width (- x2 x1)
            height (- y2 y1)
            target (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
            g (.getGraphics target)]
        (.drawImage g source 0 0 width height x1 y1 x2 y2 nil)
        (ImageIO/write target "png" ^File (child-file-fn (str xi "-" yi)))))))
