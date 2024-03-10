(ns components.message)

(defn message [body]
  [:article.message.is-warning
   [:div.message-body body]])