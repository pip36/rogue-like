(ns game.actions)

(defmulti handle :type)

(defmethod handle :default [action]
  (. js/console error "No action handler defined for: " (:type action)))

(defn trigger-actions [actions-list]
  (doseq [action actions-list]
    (handle action)))