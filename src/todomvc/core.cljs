(ns todomvc.core
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom :include-macros true]
            [todomvc.global :as global]
            [todomvc.helpers :as helpers]
            [todomvc.routes :as routes]
            
            [todomvc.components.utils :as u]
            [todomvc.components.title :as title]
            [todomvc.components.todo-input :as todo-input]
            [todomvc.components.footer :as footer]
            [todomvc.components.todos-toggle :as todos-toggle]
            [todomvc.components.todos-list :as todos-list]
            [todomvc.components.todos-count :as todos-count]
            [todomvc.components.todos-filters :as todos-filters]
            [todomvc.components.todos-clear :as todos-clear]))

(defrecord Add [text])
(defrecord Update [id todo])
(defrecord Delete [id])
(defrecord ToggleAll [value])
(defrecord ClearCompleted [])

(reacl/defclass todo-app this state [display-type]
  render
  (let [todos (:todos state)]
    (dom/div (dom/section {:class "todoapp"}
                          (dom/header {:class "header"}
                                      (title/t)
                                      (todo-input/t (u/event-message this ->Add)))
                          (dom/div {:style {:display (helpers/display-elem (helpers/todos-any? todos))}}
                                   (dom/section {:class "main"}
                                                (todos-toggle/t todos (u/event-message this ->ToggleAll))
                                                (todos-list/t todos display-type
                                                              (u/event-message this ->Update)
                                                              (u/event-message this ->Delete)))
                                   (dom/footer {:class "footer"}
                                               (todos-count/t todos)
                                               (todos-filters/t display-type)
                                               (todos-clear/t todos (reacl/return :message [this (->ClearCompleted)])))))
             (footer/t)))

  handle-message
  (fn [msg]
    (condp instance? msg
      Add
      (let [id (keyword (str "id" (:counter state)))
            title (:text msg)]
        (reacl/return :app-state
                      (-> state
                          (update :todos assoc id {:title title :completed false})
                          (update :counter inc))))

      Update
      (let [id (:id msg)
            todo (:todo msg)]
        (reacl/return :app-state
                      (-> state
                          (assoc-in [:todos id] todo))))

      Delete
      (let [id (:id msg)]
        (reacl/return :app-state
                      (-> state
                          (update :todos dissoc id))))
      
      ClearCompleted
      (reacl/return :app-state
                    (-> state
                        (assoc :todos (reduce dissoc
                                              (:todos state)
                                              (helpers/todos-completed-ids (:todos state))))))

      ToggleAll
      (let [toggle (:value msg)]
        (reacl/return :app-state
                      (-> state
                          (assoc :todos (reduce (fn [todos id]
                                                  (assoc-in todos [id :completed] toggle))
                                                (:todos state)
                                                (keys (:todos state)))))))
      
      )))

(defrecord ChangeDisplay [type])
(defrecord ChangeState [state])

(reacl/defclass todo-app-controller this state [todos-changed-action counter-changed-action]
  local-state
  [lstate {:display-type :all}]
  
  render
  (todo-app (reacl/reactive state (reacl/reaction this ->ChangeState))
            (:display-type lstate))

  handle-message
  (fn [msg]
    (condp instance? msg
      ChangeDisplay
      (let [display-type (:type msg)]
        (reacl/return :local-state (assoc lstate :display-type display-type)))

      ChangeState
      (let [new-state (:state msg)]
        (cond-> (reacl/return :app-state new-state)
          (not= (:todos new-state)
                (:todos state))
          (reacl/merge-returned (todos-changed-action (:todos new-state)))

          (not= (:counter new-state)
                (:counter state))
          (reacl/merge-returned (counter-changed-action (:counter new-state))))))))

(defn ^:export run []
  (let [app (reacl/render-component (js/document.getElementById "app")
                                    todo-app-controller
                                    (reacl/handle-toplevel-action global/handle-global-action!)
                                    {:todos @todomvc.global/todos
                                     :counter @todomvc.global/todos-counter}
                                    (comp (partial reacl/return :action) (global/reset-action todomvc.global/todos))
                                    (comp (partial reacl/return :action) (global/reset-action todomvc.global/todos-counter)))]
    (routes/setup! (fn [page]
                     (reacl/send-message! app (->ChangeDisplay page))))
    app))
