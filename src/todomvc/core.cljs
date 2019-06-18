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

(reacl/defclass todo-app this state [display-type]
  local-state
  [lstate {:editing ""}]
  
  render
  (let [todos (:todos state)]
    (dom/div (dom/section {:class "todoapp"}
                          (dom/header {:class "header"}
                                      (title/t)
                                      (-> (todo-input/t (reacl/bind-locally this :editing) ::add-todo)
                                          (reacl/handle-actions this)))
                          (dom/div {:style {:display (helpers/display-elem (helpers/todos-any? todos))}}
                                   (dom/section {:class "main"}
                                                (todos-toggle/t (reacl/bind this :todos))
                                                (todos-list/t (reacl/bind this :todos) display-type))
                                   (dom/footer {:class "footer"}
                                               (todos-count/t todos)
                                               (todos-filters/t display-type)
                                               (todos-clear/t (reacl/bind this :todos)))))
             (footer/t)))

  handle-message
  (fn [msg]
    (cond
      (= msg ::add-todo)
      (let [id (keyword (str "id" (:counter state)))
            tr (helpers/trim-title (:editing lstate))]
        (reacl/return :app-state (-> state
                                     (update :todos assoc id {:id id :title tr :completed false})
                                     (update :counter inc))
                      :local-state (assoc lstate :editing "")))
      )))

(reacl/defclass todo-app-controller this state [todos-changed-action counter-changed-action]
  local-state
  [lstate {:display-type :all}] ;; TODO: init this from the location?
  
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
