# Inkball

Inkball is a recreation of the ***Windows Vista game**, developed as part of a University of Sydney assignment. The game involves drawing lines to guide balls into the correct holes while following specific game mechanics. It features similar visuals and mechanics to the original game.

This project runs on Gradle and supports level customization through configuration files.

## Getting Started

### Prerequisites
Ensure that Gradle is installed on your system before running the project.

### Running the Game
To start the game, simply execute:
```sh
gradle run
```

### Editing and Adding Levels
- Level files can be modified to change the game experience.
- New levels can be added by creating the required JSON object in `config.json`.
- Update the `level count` variable in the `setup()` function within `App.java` to reflect the new number of levels.

### Additional Features
- The project allows for Javadoc documentation to be generated.
- `report-jacoco` is used for test coverage as part of the assignment requirements.

## Gameplay Instructions
- **Draw lines** using the left mouse button; balls will reflect off these lines.
- **Erase lines** using the right mouse button.
- **Restart** the level by pressing `R`.
- **Pause** the game by pressing the spacebar.
- The game features a time limit; running out of time results in failure.

### Scoring
- Grey balls can enter any hole, and any ball can go into a grey hole.
- Otherwise, the colour of the ball must match the colour of the hole.
- Sending a ball into the correct hole increases the score, while incorrect placements reduce it.
- The score change is determined by values in `config.json`.
- Upon completing the game, the final time is converted into a score at a rate of approximately **15 points per second** (calculated every second frame at 30 FPS).

## Level Configuration
### Understanding Level Files
Levels are defined in text files and must be of uniform size. The configuration follows this structure:

#### Ball (`B`) and Hole (`H`) Colours
- `B0` / `H0` - Grey
- `B1` / `H1` - Orange
- `B2` / `H2` - Blue
- `B3` / `H3` - Green
- `B4` / `H4` - Yellow

#### Walls (`X` and Numbers)
- Walls are represented by numbers indicating their colour with no preceding letter.
- Grey walls use `X` instead of `0` and do not change the ball’s colour upon contact.
- Walls reflect balls.

#### Acceleration Pads (`A`)
- `A1` - Accelerates balls **up**
- `A2` - Accelerates balls **right**
- `A3` - Accelerates balls **down**
- `A4` - Accelerates balls **left**

#### Spawn Points (`S`)
- `S` represents a spawn point for balls.
- The queue for spawning is defined in `config.json`.

For modifications or custom levels, refer to `config.json` and the existing level text files to ensure consistency.

---

## License
This project was created for educational purposes as part of a University of Sydney course. Since it is a recreation of the Windows Vista Inkball game, it is provided for learning and personal use only.

✅ You may:

View, modify, and share the code for personal and educational purposes.

❌ You may not:

Use the code commercially.
Claim it as your own original work.
See [LICENSE](LICENSE) for full details.