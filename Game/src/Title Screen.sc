// Dance Sounds Title Screen

// Define assets for this scene, gets loaded at `World_Assets.init`
+ World_Assets {
	titleScreen_ASSET_DEF {
		// This game doesn't have visual elements
		imagePaths = IdentityDictionary[];
		// Link to our own assets directory with the music
		bufferPaths = IdentityDictionary[
			\initialMusic -> "../../../assets/3rdparty/AwayFromKeyboard.mp3",
			\greetingVoice -> "../../../assets/voice/Greeting1.mp3"
		];
	}
}

// Define the scene for the title screen
+ World_Scene {
	*buildTitleScreenScene {
		projectName = "Dance Sounds";
		World_Scene.setupScene(26, 17, 50, 50, 1, \titleScreen_ASSET_DEF);

		// Scene initialization
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\initialMusic, 0.3, 2, 1, 1, 0, true, \music);
			}.forkInScene;
			{
				3.wait;
				World_Audio.play(\greetingVoice, 1, 0, 1, 1, 0, false, \dialog);
			}.forkInScene;
		};

		// Detect 0-9 key inputs for now to test
		// specific game states
		sceneScripts[\keyDown] = {|key|
			switch(key.key)
			// 1
			{49} { World_World.startGame(\buildLearningModeScene) }
		};

		sceneScripts[\leaveScene] = {
			World_Audio.release(\initialMusic);
			World_Audio.release(\greetingVoice);
		};
		sceneScripts[\revisitScene] = {
			World_Audio.releaseEverything;
			scenes[1..].do(_.free);
			scenes = scenes[..0];   // remove old scenes
			World_Audio.play(\initialMusic, 0.3, 2, 1, 1, 0, true, \music);
		};
	}
}
