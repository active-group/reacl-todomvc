(ns todomvc.routes
  (:require [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.History))

(def goto (atom nil))

(defn setup! [goto-fn]
  (reset! goto goto-fn)
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (@goto :all))

(secretary/defroute "/active" []
  (@goto :active))

(secretary/defroute "/completed" []
  (@goto :completed))


