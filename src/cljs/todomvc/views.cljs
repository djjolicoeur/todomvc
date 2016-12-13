(ns todomvc.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn todo-input
  [{:keys [title on-save on-stop]}]
  (let [val (reagent/atom title)
        stop #(do (reset! val "")
                  (when on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (when (seq v) (on-save v))
                (stop))]
    (fn [props]
      (println (pr-str {:props props}))
      [:input (merge props
                     {:type "text"
                      :value @val
                      :auto-focus true
                      :on-blur save
                      :on-change #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                      13 (save)
                                      27 (stop)
                                      nil)})])))

(defn todo-item []
  (let [editing (reagent/atom false)]
    (fn [{:keys [id title done]}]
      (println {:component ::todo-item :id id :title title :done done})
      [:li {:class (str (when done "completed")
                        (when @editing "editing"))}
       [:div.view
        [:input.toggle
         {:type "checkbox"
          :checked done
          :on-change #(dispatch [:toggle-done id])}]
        [:label
         {:on-double-click #(reset! editing true)}
         title]
        [:button.destroy
         {:on-click #(dispatch [:delete-todo id])}]]
       (when @editing
         [todo-input
          {:class "edit"
           :title title
           :on-save #(dispatch [:save id %])
           :on-stop #(reset! editing false)}])])))

(defn task-list
  []
  (let [visible-todos (subscribe [:visible-todos])
        all-complete? (subscribe [:all-complete?])]
    (fn []
      (println {:visible-todos @visible-todos :all-complete? @all-complete?})
      [:section#main
       [:input#toggle-all
        {:type "checkbox"
         :checked @all-complete?
         :on-change #(dispatch [:complete-all-toggle (not @all-complete?)])}]
       [:label
        {:for "toggle-all"}
        "Mark all as Complete"]
       [:ul#todo-list
        (for [todo @visible-todos]
          (do (println {:todo todo})
              ^{:key (:id todo)} [todo-item todo]))]])))

(defn footer-controls
  []
  (let [footer-stats (subscribe [:footer-counts])
        showing (subscribe [:showing])]
    (fn []
      (let [[active done] @footer-stats
            a-fn (fn [filter-kw txt]
                   [:a {:class (when (= filter-kw @showing) "selected")
                        :href (str "#/" (name filter-kw))} txt])]
        [:footer#footer
         [:span#todo-count
          [:strong active] " " (if (= active 1) "item" "items") " left"]
         [:ul#filters
          [:li (a-fn :all "All")]
          [:li (a-fn :active "Active")]
          [:li (a-fn :done "Completed")]]
         (when (pos? done)
           [:button#clear-completed {:on-click #(dispatch [:clear-completed])}
            "Clear Completed"])]))))

(defn task-entry
  []
  [:header#header
   [:h1 "todos"]
   [todo-input
    {:id "new-todo"
     :placeholder "What Needs to be Done"
     :on-save #(dispatch [:add-todo %])}]])

(defn todo-app
  []
  (let [todos (subscribe [:todos])]
    (fn []
      [:div
       [:section#todoapp
        [task-entry]
        (when (seq @todos)
          (println {:todos @todos})
          [task-list])
        [footer-controls]]
       [:footer#info
        [:p "Double-click to edit a todo"]]])))
