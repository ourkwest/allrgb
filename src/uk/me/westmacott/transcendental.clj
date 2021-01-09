(ns uk.me.westmacott.transcendental)


(def TAU (* 2 Math/PI))

(defn taus
  ([n] (* TAU n))
  ([a b] (* TAU a b))
  ([a b & cs] (apply * TAU a b cs)))
