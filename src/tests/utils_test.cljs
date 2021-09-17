(ns tests.utils-test
  (:require [cljs.test :refer (deftest run-tests are)]
            [game.state :as utils]))


(deftest coordinates->i_converts_tilemap_coordinates_to_an_index_value
  (are [expected map_size x y] (= expected (utils/coordinates->i map_size x y))
    0 1 0 0
    2 2 0 1
    1 2 1 0
    3 2 1 1
    8 3 2 2))

