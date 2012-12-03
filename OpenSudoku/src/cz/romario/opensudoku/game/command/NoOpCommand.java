package cz.romario.opensudoku.game.command;

public class NoOpCommand extends AbstractCommand {

	public NoOpCommand(){
		// nothing to initialize 
	}
	
	@Override
	void execute() {
		// do nothing
	}

	@Override
	void undo() {
		// nothing to undo
	}

}
