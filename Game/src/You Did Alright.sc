// You Did Alright scene

+ World_Assets {
	youDidAlright_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\youDidAlrightMusic -> "../../../assets/3rdparty/YouDidAlright.mp3",
			\notTooBadVoice -> "../../../assets/voice/NotTooBad.mp3"
		];
	}
}

+ World_Scene {
	*buildYouDidAlrightScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \youDidAlright_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\youDidAlrightMusic, 1, 2, 1, 1, 0, true, \music)
			}.forkInScene;
			{
				3.wait;
				World_Audio.play(\notTooBadVoice, 6, 0, 1, 1, 0, false, \dialog);
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
			World_Audio.release(\youDidAlrightMusic);
			World_Audio.release(\notTooBadVoice);
		};
	}
}
