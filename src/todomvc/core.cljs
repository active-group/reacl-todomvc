(ns todomvc.core
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom :include-macros true]
            [todomvc.session :as session]
            [todomvc.helpers :as helpers]
            [todomvc.routes :as routes]
            
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
    (-> (dom/div (dom/section {:class "todoapp"}
                              (dom/header {:class "header"}
                                          (title/t)
                                          (todo-input/t ->Add))
                              (dom/div {:style {:display (helpers/display-elem (helpers/todos-any? todos))}}
                                       (dom/section {:class "main"}
                                                    (todos-toggle/t todos ->ToggleAll)
                                                    (todos-list/t todos display-type ->Update ->Delete))
                                       (dom/footer {:class "footer"}
                                                   (todos-count/t todos)
                                                   (todos-filters/t display-type)
                                                   (todos-clear/t todos (->ClearCompleted)))))
                 (footer/t))
        (reacl/handle-actions this)))

  handle-message
  (fn [msg]
    (condp instance? msg
      Add
      (let [id (keyword (str "id" (:counter state)))
            tr (helpers/trim-title (:text msg))]
        (reacl/return :app-state
                      (-> state
                          (update :todos assoc id {:title tr :completed false})
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

(reacl/defclass todo-app-controller this state [todos-changed-action counter-changed-action]
  local-state
  [lstate {:display-type :all}]
  
  render
  (todo-app (reacl/reactive state (reacl/pass-through-reaction this))
            (:display-type lstate))

  handle-message
  (fn [msg]
    (cond
      (= (first msg) ::goto)
      (let [display-type (second msg)]
        (reacl/return :local-state (assoc lstate :display-type display-type)))
      :else
      (let [new-state msg]
        (cond-> (reacl/return :app-state new-state)
          (not= (:todos new-state)
                (:todos state))
          (reacl/merge-returned (reacl/return :action todos-changed-action))

          (not= (:counter new-state)
                (:counter state))
          (reacl/merge-returned (reacl/return :action counter-changed-action)))))))

(defn ^:export run []
  (let [app (reacl/render-component (js/document.getElementById "app")
                                    todo-app-controller
                                    (reacl/handle-toplevel-actions
                                     (fn [state act]
                                       (cond
                                         (= act ::todos-changed)
                                         (do (reset! todomvc.session/todos (:todos state))
                                             (reacl/return))

                                         (= act ::counter-changed)
                                         (do (reset! todomvc.session/todos-counter (:counter state))
                                             (reacl/return)))))
                                    {:todos @todomvc.session/todos
                                     :counter @todomvc.session/todos-counter}
                                    ::todos-changed
                                    ::counter-changed)]
    (routes/setup! (fn [page]
                     (reacl/send-message! app [::goto page])))
    app))
