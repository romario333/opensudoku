package cz.romario.opensudoku.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import cz.romario.opensudoku.gui.FolderListActivity;
import cz.romario.opensudoku.gui.SudokuExportActivity;
import cz.romario.opensudoku.gui.SudokuListActivity;
import cz.romario.opensudoku.R;


public class FolderListActivityTest extends
		ActivityInstrumentationTestCase2<FolderListActivity> {

	static final String NEW_FOLDER_NAME = "Robotest";
	static final String RENAMED_FOLDER_NAME = "RenamedTest";	
	
	private Solo solo;

	public FolderListActivityTest() {
		super("cz.romario.opensudoku", FolderListActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	public void testListItemSelect() {
		solo.clickInList(2);
		solo.assertCurrentActivity("SudokuListActivity", SudokuListActivity.class);
		String medium = getString(R.string.difficulty_medium);
		assertTrue(solo.getCurrentActivity().getTitle().toString().contains(medium));
	}
	
	public void testListItemExport() {
		String medium = getString(R.string.difficulty_medium);
		solo.clickLongOnText(medium);
		solo.clickOnText(getString(R.string.export_folder));
		solo.assertCurrentActivity("SudokuExportActivity", SudokuExportActivity.class);
		assertTrue(solo.searchText(medium));
	}
	
	public void testMenuExportAllFolders() {
		solo.clickOnMenuItem(getString(R.string.export_all_folders));
		solo.assertCurrentActivity("SudokuExportActivity", SudokuExportActivity.class);
	}
	
	public void testMenuAbout() {
		solo.clickOnMenuItem(getString(R.string.about));
		assertTrue(solo.searchText("Version:"));
		solo.clickOnButton(getString(android.R.string.ok));
	}

	public void testGetPuzzlesOnline() {
		solo.clickOnButton(getString(R.string.get_more_puzzles_online));
		solo.waitForText("opensudoku-android");
	}
	
	
	
	public void test01MenuAddFolder() throws InterruptedException {
		// create test folder
		solo.clickOnMenuItem(getString(R.string.add_folder));
		solo.enterText(0, NEW_FOLDER_NAME);
		solo.clickOnButton(getString(R.string.save));
		solo.searchText(NEW_FOLDER_NAME);
		
		// click on our newly created folder
		solo.clickInList(getActivity().getListView().getCount());
		solo.assertCurrentActivity("SudokuListActivity", SudokuListActivity.class);
		assertTrue(solo.getCurrentActivity().getTitle().toString().contains(NEW_FOLDER_NAME));
	}
	
	public void test02ListItemRenameFolder() {
		solo.clickLongOnText(NEW_FOLDER_NAME);
		solo.clickOnText(getString(R.string.rename_folder));
		solo.clearEditText(0);
		solo.enterText(0, RENAMED_FOLDER_NAME);
		solo.clickOnButton(getString(R.string.save));
		
		assertTrue(solo.searchText(RENAMED_FOLDER_NAME));
		assertFalse(solo.searchText(NEW_FOLDER_NAME));
	}
	
	public void test03ListItemDeleteFolder() throws InterruptedException {
		// select folder created in testMenuAddFolder and delete it
		solo.clickLongOnText(RENAMED_FOLDER_NAME);
		solo.clickOnText(getString(R.string.delete_folder));
		solo.clickOnButton(getString(android.R.string.ok));
		
		assertFalse(solo.searchText(RENAMED_FOLDER_NAME));
	}
	

	@Override
	protected void tearDown() throws Exception {
		try {
			solo.finalize();
		} catch (Throwable e) {

			e.printStackTrace();
		}
		getActivity().finish();
		super.tearDown();

	}
	
	private void addFolder(String folderName) {
		
	}
	
	private String getString(int resId) {
		return getActivity().getString(resId);
	}
	

}
