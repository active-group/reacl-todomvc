(ns todomvc.components.todos-list
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.todo-item :as todo-item]))

(reacl/defclass t this todos [display-type]
  render
  (dom/ul {:class "todo-list"}
          (for [[id todo] todos]
            (-> (todo-item/t (reacl/bind this id) display-type [::delete id])
                (reacl/keyed id)
                (reacl/handle-actions this))))

  handle-message
  (fn [msg]
    (cond
      (= (first msg) ::delete)
      (reacl/return :app-state (dissoc todos (second msg))))))
