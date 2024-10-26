// Game engine
Game {
	// Public variables
    var <window;

    // Constructor
    *new { |width = 800, height = 600|
        ^super.new.init(width, height);
    }

    init { |width, height|
        window = Window("Dance Sound", Rect(100, 100, width, height)).front;
		window.background = Color.black;
    }
}
