// You Crushed It scene

+ World_Assets {
	youCrushedIt_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\youCrushedItMusic -> "../../../assets/3rdparty/YouCrushedIt.mp3",
			\youCrushedItVoice -> "../../../assets/voice/YouCrushedIt.mp3"
		];
	}
}

+ World_Scene {
	*buildYouCrushedItScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \youCrushedIt_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\youCrushedItMusic, 0.1, 2, 1, 1, 0, true, \music)
			}.forkInScene;
			{
				3.wait;
				World_Audio.play(\youCrushedItVoice, 1, 0, 1, 1, 0, false, \dialog);
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

		sceneScripts[\leaveScene] = {
			World_Audio.release(\youCrushedItMusic);
			World_Audio.release(\youCrushedItVoice);
		};
	}
}
