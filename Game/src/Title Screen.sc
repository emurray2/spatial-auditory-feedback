// Dance Sounds Title Screen

// Define assets for this scene, gets loaded at `World_Assets.init`
+ World_Assets {
	titleScreen_ASSET_DEF {
		// This game doesn't have visual elements
		imagePaths = IdentityDictionary[];
		// Link to our own assets directory with the music
		bufferPaths = IdentityDictionary[\initialMusic -> "../../../assets/3rdparty/AwayFromKeyboard.mp3"];
	}
}

// Define the scene for the title screen
+ World_Scene {
	*buildTitleScreenScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \titleScreen_ASSET_DEF);
		{
			1.wait;
			World_Audio.play(\initialMusic, 1, 5, 1, 1, 0, true, \music);
		}.forkInScene
	}
}
