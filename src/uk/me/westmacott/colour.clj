(ns uk.me.westmacott.colour)






(defmacro delayed-iterable [form]
  `(let [d# (delay ~form)]
     (reify Iterable
       (iterator [_this#]
         (.iterator ~form)))))


(defn shuffled-colours [colours])


