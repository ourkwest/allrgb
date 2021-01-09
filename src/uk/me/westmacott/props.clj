(ns uk.me.westmacott.props
  (:require
    [uk.me.westmacott.transcendental :refer :all]))


(defn prange [steps]
  (range 0 1 (/ 1 steps)))

(defn lerp [a b p]
  (+ (* (- 1.0 p) a) (* p b)))

(defn compose [cf wf1 wf2]
  #(cf (wf1 %) (wf2 %)))

(defn add [& wfs] (reduce (partial compose +) wfs) #_(compose + wf1 wf2))
(defn sub [& wfs] (reduce (partial compose -) wfs) #_(compose + wf1 wf2))
(defn mult [& wfs] (reduce (partial compose *) wfs) #_(compose * wf1 wf2))

(defn backwards [wf]
  (fn [p]
    (wf (- 1 p))))

(defn const [w]
  (constantly w))

(defn straight [w1 w2]
  #(lerp w1 w2 %))

(defn ease-out
  ([wf]
   (mult (ease-out) wf))
  ([]
   (fn [p]
     (/ (inc (Math/cos (taus p 1/2))) 2))))

(defn ease-in
  ([wf]
   (mult (ease-in) wf))
  ([]
   (backwards (ease-out))))

(defn sin
  ([wf]
   (mult (sin) wf))
  ([]
   (fn [p]
     (/ (- 1 (Math/cos (taus p))) 2))))

(defn bulge
  ([wf]
   (mult (bulge) wf))
  ([]
   (fn [p]
     (Math/sin (taus p 1/2)))))

(defn bend [wf bend-factor]
  #(wf (Math/pow % bend-factor)))



