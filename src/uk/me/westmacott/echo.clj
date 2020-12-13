(ns uk.me.westmacott.echo
  (:import [uk.me.westmacott Echo Constants]
           [java.awt Point]))


(def noop-echo (Echo/NoopEcho))

(defn make-echo
  ([width height buffer-size x-offset y-offset]
   (make-echo width height buffer-size x-offset y-offset noop-echo))
  ([width height buffer-size x-offset y-offset child-echo] ; wrap?
   (let [buffer-size (int buffer-size)
         not-yet-used -1
         xs (int-array buffer-size not-yet-used)
         ys (int-array buffer-size not-yet-used)
         pointer (volatile! 0)]
     (reify Echo
       (echo [_ canvas point]
         (let [pointer-location @pointer
               old-x (aget xs pointer-location)
               old-y (aget ys pointer-location)
               echo-x (+ old-x x-offset)
               echo-y (+ old-y y-offset)]
           (when (not= not-yet-used old-x)
             (.echo child-echo canvas (Point. echo-x echo-y))
             (when (and (< -1 echo-x width)
                        (< -1 echo-y height)
                        (= Constants/UNSET (aget canvas echo-x echo-y)))
               (aset canvas echo-x echo-y (aget canvas old-x old-y))))
           (aset xs pointer-location (.-x point))
           (aset ys pointer-location (.-y point)))
         (vswap! pointer #(-> % inc (mod buffer-size)))
         )))))

(defn multi-echo [& echos]
  (reify Echo
    (echo [_ canvas point]
      (doseq [child echos]
        (.echo ^Echo child canvas point)))))
