(ns game.actions
  (:require [game.potion-behaviours :as potion]
            [game.state :refer [next-level
                                set-entity-position
                                get-item
                                consume-item
                                get-entity
                                dropped-items
                                entities
                                add-event
                                get-entity-at
                                set-entity-direction
                                tile-is?
                                get-player-tile-infront
                                open-door
                                open-chest
                                update-entity-stat
                                kill-entity]]))

(defn trigger-item-effects [entity-id item-id]
  (doseq [effect (:effects (get-item entity-id item-id))]
    (potion/do-effect entity-id effect)))

(defn use-item [entity-id item-id]
  (trigger-item-effects entity-id item-id)
  (consume-item entity-id item-id))

(defn pickup-action [entity-id]
  (let [entity (get-entity entity-id)
        x (:x entity)
        y (:y entity)
        item-list (@dropped-items [x y])
        item-map (reduce (fn [acc x]
                           (assoc acc (:id x) x)) {} item-list)]
    (swap! entities update-in [entity-id :items] merge item-map)
    (swap! dropped-items dissoc [x y])
    (add-event (str "Picked up " (map :name item-list)))))

(defn attack-action [src-id target-id]
  (let [{src-name :display-name
         damage :attack} (get-entity src-id)
        {target-name :display-name} (get-entity target-id)]
    (update-entity-stat target-id :health (- damage))
    (add-event (str src-name " hits " target-name " for " damage " damage!"))
    (when (<= (:health (get-entity target-id)) 0) (kill-entity target-id))))

(defn move-action [[x y] direction]
  (let [entity (get-entity-at x y)]
    (set-entity-direction :player direction)
    (cond
      (some? entity) (attack-action :player (:id entity))
      (tile-is? :STAIRS x y) (next-level)
      (tile-is? :BLANK x y) (set-entity-position :player x y)
      :else nil)))

(defn open-action []
  (let [[x y] (get-player-tile-infront)]
    (cond
      (tile-is? :DOOR x y) (open-door x y)
      (tile-is? :CLOSED-CHEST x y) (open-chest x y))))

(defn hunger-trigger [entity-id]
  (let [entity (get-entity entity-id)]
    (update-entity-stat entity-id :food -1)
    (when (<= (:food (get-entity entity-id)) 0)
      (kill-entity entity-id)
      (add-event (str (:display-name entity) " died from hunger.")))))