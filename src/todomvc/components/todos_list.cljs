(ns todomvc.components.todos-list
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.components.todo-item :as todo-item]))

(defrecord Update [todo id])

(reacl/defclass t this [todos display-type update-action delete-action]
  render
  (dom/ul {:class "todo-list"}
          (for [[id todo] todos]
            (-> (todo-item/t (reacl/reactive todo (reacl/reaction this ->Update id))
                             display-type (partial delete-action id))
                (reacl/keyed id))))

  handle-message
  (fn [msg]
    (condp instance? msg
      Update (update-action (:id msg) (:todo msg)))))
