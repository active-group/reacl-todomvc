(ns todomvc.components.todos-clear
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.utils :as u]
            [todomvc.helpers :as helpers]))

(reacl/defclass t this todos []
  render
  (-> (u/button {:class "clear-completed"
                 :style {:display (helpers/display-elem (helpers/todos-any-completed? todos))}
                 :onclick ::click}
                "Clear completed")
      (reacl/handle-actions this))
  
  handle-message
  (fn [msg]
    (cond
      (= ::click msg)
      ;; TODO: drop need for having :id in todo.
      (if-let [to-delete (not-empty (map :id (helpers/todos-completed todos)))]
        (reacl/return :app-state (apply dissoc todos to-delete))
        (reacl/return)))))
