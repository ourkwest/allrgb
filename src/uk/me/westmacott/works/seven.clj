(ns uk.me.westmacott.works.seven
  (:require
    [uk.me.westmacott.core :as core]
    [uk.me.westmacott.echo :as echo])
  (:import
    [uk.me.westmacott Constants]
    [java.awt Color]))

(def these-colours
  (map int (range Constants/PIXEL_COUNT)))

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
  (let [x (int (* width 5/6))
        y (int (* height 5/6))]
    (fn [_canvas available]
      (.add available Color/BLACK x y)
      available)))

(defn build-echos [width height]
  (let [make-echo #(echo/make-echo width height (* Constants/PIXEL_COUNT %3) (* width %1) (* height %2))
        echo-0-1 (make-echo 0 -1/3 1/9)
        echo-0-2 (make-echo 0 -2/3 2/9)
        echo-1-0 (make-echo -1/3 0 3/9)
        echo-1-1 (make-echo -1/3 -1/3 4/9)
        echo-1-2 (make-echo -1/3 -2/3 5/9)
        echo-2-0 (make-echo -2/3 0 6/9)
        echo-2-1 (make-echo -2/3 -1/3 7/9)
        echo-2-2 (make-echo -2/3 -2/3 8/9)]
    (echo/multi-echo          echo-0-1 echo-0-2
                     echo-1-0 echo-1-1 echo-1-2
                     echo-2-0 echo-2-1 echo-2-2)))

(defn render []
  (let [width (* 3 Constants/IMAGE_SIZE)
        height (* 3 Constants/IMAGE_SIZE)
        colours #(partial-shuffle (reverse these-colours) 1/10)
        canvas-prep (canvas-preparer width height)
        random-seed 1
        echo (build-echos width height)]

    #_(masking/preview-masking width height canvas-prep 1)

    (core/render width height colours canvas-prep
                 :random-seed random-seed
                 :wrap true
                 :echo echo)))