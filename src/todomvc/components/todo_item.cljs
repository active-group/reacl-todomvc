(ns todomvc.components.todo-item
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.helpers :as helpers]
            [todomvc.components.utils :as u]
            [todomvc.components.todo-edit :as todo-edit]))

(defn todo-item-class [completed editing]
  (str (when completed "completed ")
       (when editing "editing")))

(defrecord Edit [])
(defrecord Commit [])
(defrecord Reset [])

(reacl/defclass t this todo [display-type delete-action]
  local-state [editing nil] ;; nil or text while editing
  
  render
  (let [{:keys [title completed]} todo]
    (dom/li {:class (todo-item-class completed editing)
             :style {:display (helpers/display-item
                               (helpers/todo-display-filter completed display-type))}}
            (dom/div {:class "view"}
                     (u/checkbox (reacl/bind this :completed)
                                 {:class "toggle"})
                     (u/label {:ondoubleclick (reacl/return :message [this (->Edit)])}
                              title)
                     (u/button {:class "destroy"
                                :onclick delete-action}))
            (todo-edit/t (reacl/bind-locally this)
                         (reacl/return :message [this (->Commit)])
                         (reacl/return :message [this (->Reset)]))))

  handle-message
  (fn [msg]
    (condp instance? msg
      Commit
      (if-let [tr (not-empty (helpers/trim-title editing))]
        (reacl/return :app-state (assoc todo :title tr)
                      :local-state nil)
        delete-action)

      Reset
      (reacl/return :local-state nil)
      
      Edit
      (reacl/return :local-state (:title todo)))))
