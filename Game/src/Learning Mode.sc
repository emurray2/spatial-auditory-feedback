// Learning mode scene

+ World_Assets {
	learningMode_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\learningMusic -> "../../../assets/3rdparty/LearningMode.mp3"
		];
	}
}

+ World_Scene {
	*buildLearningModeScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \learningMode_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\learningMusic, 1, 2, 1, 1, 0, true, \music)
			}.forkInScene;
		};

		sceneScripts[\keyDown] = {|key|
			switch(key.key)
			{49} {
				World_Scene.createScene(\buildPracticeModeScene);
			}
			// Return
			{16777220} {
				World_Audio.stopEverything;
				World_World.quitGame;
			}
		};

		sceneScripts[\leaveScene] = { World_Audio.release(\learningMusic) };
	}
}
