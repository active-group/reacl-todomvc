(ns todomvc.components.todo-item
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom]
            [todomvc.helpers :as helpers]
            [todomvc.components.utils :as u]
            [todomvc.components.todo-edit :as todo-edit]))

(defn todo-item-class [completed editing]
  (str (when completed "completed ")
       (when editing "editing")))

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
                     (dom/label {:ondoubleclick (fn [_] (reacl/send-message! this ::edit))}
                                title)
                     (u/button {:class "destroy"
                                :onclick delete-action}))
            (-> (todo-edit/t (reacl/bind-locally this) ::commit ::reset)
                (reacl/handle-actions this))))

  handle-message
  (fn [msg]
    (cond
      (= msg ::commit)
      (let [tr (helpers/trim-title editing)]
        (if-not (empty? tr)
          (reacl/return :app-state (assoc todo :title tr)
                        :local-state nil)
          (reacl/return :action delete-action)))

      (= msg ::reset)
      (reacl/return :local-state nil)
      
      (= msg ::edit)
      (reacl/return :local-state (:title todo)))))
