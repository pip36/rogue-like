(ns game.core
  (:require
   [game.canvas :as canvas]
   [game.state :as s]
   [game.config :as config]))

(defn render []
  (canvas/clear)
  (s/render-map)
  (s/render-player)
  (s/render-monsters))

(defn register-input-listener []
  (set! (. js/document -onkeydown)
        (fn [e]
          (let [k (config/input-keys (. e -keyCode))]
            (s/handle-user-update k)
            (render)
            (s/update-monsters)
            (render)))))

(defn populate-map []
  (reset! s/monsters {:1 (s/create-jelly {:id :1 :x 13 :y 13})
                      :2 (s/create-jelly {:id :2 :x 1 :y 1})
                      :3 (s/create-jelly {:id :3 :x 6 :y 6})
                      :4 (s/create-statue {:id :4 :x 9 :y 10})}))

(defn start-game []
  (canvas/init-ctx)
  (register-input-listener)
  (populate-map)
  (render))
