// Practice mode scene

+ World_Assets {
	practiceMode_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\practiceMusic -> "../../../assets/3rdparty/PracticeMode.mp3"
		];
	}
}

+ World_Scene {
	*buildPracticeModeScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \practiceMode_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\practiceMusic, 1, 2, 1, 1, 0, true, \music)
			}.forkInScene;
		};

		sceneScripts[\keyDown] = {|key|
			switch(key.key)
			// Return
			{16777220} {
				World_Audio.stopEverything;
				World_World.quitGame;
			}
		};

		sceneScripts[\leaveScene] = { World_Audio.release(\practiceMusic) };
	}
}
