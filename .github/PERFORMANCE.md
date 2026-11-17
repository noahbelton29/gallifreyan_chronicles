# Checking performance with spark

spark is a mod that shows what the game is spending its time on. It's already on the test server.
You use it to compare the game before your change and after it.

## The main rule: test the same thing both times

The numbers only mean something if both tests are done the same way. We can't reset the server world each time,
so instead keep the test as similar as you can:

- Stand in the same place, looking the same way (for example, at the console of your TARDIS).
- Do the same thing both times (flying, landing, standing still, walking through the door).
- Try to test with about the same number of players online.
- Wait about 30 seconds after joining before you start, so everything has loaded.
- Test for the same length of time both times.

Write down what you did in the PR under "Where I tested", so the other person knows.

If you want really exact numbers, download a copy of the server world once and test in singleplayer with it.
That doesn't touch the real server.

## Before and after

1. **Before:** test with the current build on the server (the one without your change).
2. **After:** put your PR's jar on the server (the download link is in the comment on your PR) and test again.
3. Put both numbers in the table.

## Server commands

Start the profiler for 2 minutes:

```
/spark profiler start --timeout 120
```

When it finishes, it gives you a link. Open it and look for:

- **MSPT** at the top. This is how long each tick takes, in milliseconds. Lower is better.
  Above 50 means the server is lagging.
- **Our mod's share:** click the **Sources** view. It shows how much of the time each mod uses.
  Write down the number for gallifreyan_chronicles.

For a quick check without a full profile:

```
/spark health
```

## Client commands

For things you see on screen (rendering, models, effects), use the client version:

```
/sparkc profiler start --timeout 60
```

and write down your FPS from the F3 screen.

## Tips

- One test can be a bit random. If the difference is small, test two or three times and use the average.
- Put both spark links in the PR. They stay online, so the other person can look at them too.
