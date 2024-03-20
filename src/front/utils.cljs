(ns utils)

(defn cancel-event
  "Apply `preventDefault` and `stopPropagation` to the given *js Event*"
  [e]
  (.preventDefault e)
  (.stopPropagation e))