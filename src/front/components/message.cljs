(ns components.message)

(defn warning-message [body]
  [:article.message.is-warning
   [:div.message-body body]])


(defn error-message [body]
  [:article.message.is-danger
   [:div.message-body body]])

(defn info-message [body]
  [:article.message.is-info
   [:div.message-body body]])
