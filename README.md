# Fruitbridge by AbyssalMC
### **Fruitbridging might be more sophisticated than you think...**
Modrinth download: [Fruitbridge Mod](https://modrinth.com/mod/fruitbridge)
| Official discord: [Fruitbridging Tierlists Discord](https://discord.gg/4E7zUVPEzH) | Youtube: [AbyssalMC](https://youtube.com/@AbyssalMC)

This mod provides many fruitbridging utilities from ranked methods to the distance leaderboard to casual play.

**Distance utilities**
- By default disabled, and can be enabled using /distanceutils enable.
- This mod detects when you start bridging and when you fail, counting how far you go.
- The distance hud can be toggled using /distancehud.
- It also detects how close placed (on average) to the center of the block face for both the first and second blocks, which is displayed in the summary each time you fail.

**Personal bests**
- This mod stores both your session PB and local PB
- This can be viewed in the PB hud, which can be enabled through /pbhud enable.
- The session PB is reset each session, whereas the local PB remains.
- To manually read or write to these PBs, use /pb [session/local] [read/write]

**Metronome**
- A metronome is also included, which may further be set to asymmetric for methods with irregular click patterns.
- To enable it, use /metronome set 6
- For asymmetrical metronomes, use /metronome set 6-4-2 where each number is the number of ticks before the next beat plays (note that in each jump there are 12 ticks)
- To stop the metronome, use /metronome stop.

**Autohotkey**
- Some methods, such as Detector Rail fruitbridge require a lot of hotkeys which may be difficult to execute when starting out.
- To practice this method without hotkeys, execute /autohotkey set 1-4-9, which swaps between those hotkeys after each place.
- To stop using it, run /autohotkey stop.

**Setup generation**
- Fruitbridge methods, particularly rainbow ones, can be tedious to build and set up.
- To automate this process, the /setupgen [id] command can be used, which automatically builds the setup based on the method id.
- To find these ids, visit the Fruitbridging Tierlists Spreadsheet, which can be accessed through our discord above or the /tierlist command.
- More info on the autogen list world and setup id syntax is avaliable on the tierlist spreadsheet.

**Tierlist integration**
- The fruitbridging tierlist contains all player rankings for Java 12b and Distance, as well as every method and their setup ids.
- The tierlist can be accessed through /tierlist.

**Autogen list world**
- Mainly for developer use, generates the entire fruitbridging list map from the data file in the src code.
- More info on the autogen list world and setup id syntax is avaliable on the tierlist spreadsheet.

![mod icon](https://cdn.modrinth.com/data/cached_images/b93da22fbdc2cd5dcb7e87bd0fea016c5d103f9a.png)

