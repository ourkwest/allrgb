(ns uk.me.westmacott.works.eight
  (:require
    [uk.me.westmacott.colour-sorters :as colour-sorters]
    [uk.me.westmacott.masking :as masking]
    [uk.me.westmacott.core :as core]
    [clojure.java.io :as io])
  (:import
    [java.awt Color BasicStroke Graphics2D]
    [uk.me.westmacott Constants ImageSpitter]
    [kroo GifSequenceWriter]
    [javax.imageio.stream FileImageOutputStream]
    [java.awt.image BufferedImage]
    [javax.imageio ImageIO]
    [java.io File]))







#_(def these-colours
  (map int (range Constants/PIXEL_COUNT)))

(def these-colours
  (colour-sorters/colours-closest-to
    Color/PINK
    (/ (* 1080 1080) Constants/PIXEL_COUNT)))

(defn partial-shuffle [coll proportion]
  (println "shuffling...")
  (let [arr (to-array coll)
        size (count arr)]
    (doseq [n (range 0 size (/ 1 proportion))]
      (let [i (int n)
            j (rand-int size)
            el-at-i (aget arr i)
            el-at-j (aget arr j)]
        (aset arr i el-at-j)
        (aset arr j el-at-i)))
    (println "shuffled!")
    (vec arr)))

(defn canvas-preparer [width height]
  (let [x (int (* width 1/2))
        y (int (* height 1/2))
        square-size (int (* width 1/8))
        half-square-size (* square-size 1/2)]
    (fn [canvas available]

      (masking/with-masking-graphics [^Graphics2D g canvas]
        (masking/set-masking g)
        (.setStroke g (BasicStroke. 10))
        (doseq [sx [(* width 3/8) (* width 5/8)]
                sy [(* height 3/8) (* height 5/8)]]
          (.fillRect g
                     (- sx half-square-size) (- sy half-square-size)
                     square-size square-size)))

      (.add available Color/BLACK x y)
      available)))

(defn write-gif [filename buffered-images]
  (let [ios (FileImageOutputStream. (io/file filename))
        gsw (GifSequenceWriter. ios (.getType ^BufferedImage (first buffered-images)) (int 5) true)]

    (doseq [image buffered-images]
      (.writeToSequence gsw image))

    (.close gsw) ; TODO: closeable???
    (.close ios)))

(defn render []
  (let [width 1080
        height 1080
        colours #(partial-shuffle (reverse these-colours) 1/10)
        canvas-prep (canvas-preparer width height)
        random-seed 1
        frames 80
        spitter (ImageSpitter/onceEvery (int (/ (* width height) frames))
                                        (ImageSpitter/forDirectory "gifs/eight" false))]

    #_(masking/preview-masking width height canvas-prep 1)

    (doseq [file (file-seq (io/file "gifs/eight"))
            :when (.isFile file)]
      (io/delete-file file :silently!))

    (core/render width height colours canvas-prep
                 :random-seed random-seed
                 :spitter spitter
                 :wrap false)

    (let [files (file-seq (io/file "gifs/eight"))
          frames (filter #(->> % str (re-matches #".*/Render_[0-9]+-[0-9]+.png")) files)
          mask (first (filter #(->> % str (re-matches #".*/Render_[0-9]+-mask.png")) files))
          composite-fn (fn [^File file]
                         (let [frame (ImageIO/read ^File mask)
                               g (.getGraphics frame)]
                           ;(println (str file))
                           (.drawImage g (ImageIO/read file) 0 0 nil)
                           frame))]
      (write-gif "gifs/eight/eight.gif"
                 (->> frames
                      (sort-by str)
                      (map composite-fn))))

    ))

