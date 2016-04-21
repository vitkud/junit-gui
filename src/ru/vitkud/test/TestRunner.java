package ru.vitkud.test;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;

public class TestRunner {

	private String suiteName;
	private Class<?> testClass;
	
	protected Shell shell;
	private ToolBar toolBar;
	private Table tableResults;
	private Table tableFailureList;
	private Tree testTree;

	/**
	 * Launch the application.
	 * @param args
	 */	
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.out.println("Missing Test class name");
				System.exit(2);
			}
			TestRunner window = new TestRunner(args[0]);
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TestRunner(String suiteName) throws ClassNotFoundException {
		this.suiteName = suiteName;
		testClass = Class.forName(suiteName);
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();

		fillTestSuite();

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setMinimumSize(new Point(300, 200));
		shell.setSize(500, 500);
		shell.setText("JUnit GUI - " + suiteName);
		shell.setLayout(new FormLayout());

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("&File");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem mntmSaveConfiguration = new MenuItem(menu_1, SWT.NONE);
		mntmSaveConfiguration.setAccelerator(SWT.CTRL | 'S');
		mntmSaveConfiguration.setText("&Save Configuration" + "\tCtrl+S");

		MenuItem mntmRestoreSavedConfiguration = new MenuItem(menu_1, SWT.NONE);
		mntmRestoreSavedConfiguration.setText("&Restore Saved Configuration");

		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.setAccelerator(SWT.ALT | 'X');
		mntmExit.setText("E&xit" + "\tAlt+X");
		
		MenuItem mntmTestTree = new MenuItem(menu, SWT.CASCADE);
		mntmTestTree.setText("Test &Tree");
		
		Menu menu_2 = new Menu(mntmTestTree);
		mntmTestTree.setMenu(menu_2);
		
		MenuItem mntmSelectAll = new MenuItem(menu_2, SWT.NONE);
		mntmSelectAll.setAccelerator(SWT.CTRL | SWT.ALT | 'A');
		mntmSelectAll.setText("Select &All" + "\tCtrl+Alt+A");
		
		MenuItem mntmDeselectAll = new MenuItem(menu_2, SWT.NONE);
		mntmDeselectAll.setAccelerator(SWT.CTRL | SWT.DEL);
		mntmDeselectAll.setText("&Deselect All" + "\tCtrl+Del");
		
		MenuItem mntm = new MenuItem(menu_2, SWT.NONE);
		mntm.setAccelerator(SWT.CTRL | SWT.ALT | 'A');
		mntm.setText("Select &All" + "\tCtrl+Alt+A");
		
		MenuItem mntmSelectFailed = new MenuItem(menu_2, SWT.NONE);
		mntmSelectFailed.setAccelerator(SWT.CTRL | 'F');
		mntmSelectFailed.setText("Select Fai&led" + "\tCtrl+F");
		
		MenuItem mntmSelectCurrent = new MenuItem(menu_2, SWT.NONE);
		mntmSelectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | 'A');
		mntmSelectCurrent.setText("Select &Current" + "\tShift+Ctrl+A");
		
		MenuItem mntmDeselectCurrent = new MenuItem(menu_2, SWT.NONE);
		mntmDeselectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | SWT.DEL);
		mntmDeselectCurrent.setText("Deselect C&urrent" + "\tShift+Ctrl+Del");
		
		new MenuItem(menu_2, SWT.SEPARATOR);
		
		MenuItem mntmHideTestNodes = new MenuItem(menu_2, SWT.NONE);
		mntmHideTestNodes.setAccelerator(SWT.CTRL | 'H');
		mntmHideTestNodes.setText("&Hide Test Nodes" + "\tCtrl+H");
		
		MenuItem mntmExpandAll = new MenuItem(menu_2, SWT.NONE);
		mntmExpandAll.setAccelerator(SWT.CTRL | 'P');
		mntmExpandAll.setText("Ex&pand All" + "\tCtrl+P");
		
		new MenuItem(menu_2, SWT.SEPARATOR);

		MenuItem mntmGoToNextSelectedNode = new MenuItem(menu_2, SWT.NONE);
		mntmGoToNextSelectedNode.setAccelerator(SWT.ALT | SWT.DOWN);
		mntmGoToNextSelectedNode.setText("Go To Next Selected Test" + "\tAlt+Down");
		
		MenuItem mntmGoToPreviousSelectedNode = new MenuItem(menu_2, SWT.NONE);
		mntmGoToPreviousSelectedNode.setAccelerator(SWT.ALT | SWT.UP);
		mntmGoToPreviousSelectedNode.setText("Go To Previous Selected Test" + "\tAlt+Up");
		
		new MenuItem(menu_2, SWT.SEPARATOR);

		MenuItem mntmCopytestnametoclipboard = new MenuItem(menu_2, SWT.NONE);
		mntmCopytestnametoclipboard.setAccelerator(SWT.CTRL | SWT.ALT | 'C');
		mntmCopytestnametoclipboard.setText("Copy testname to clipboard" + "\tCtrl+Alt+C");
		
		new MenuItem(menu_2, SWT.SEPARATOR);

		MenuItem mntmRunSelectedTest = new MenuItem(menu_2, SWT.NONE);
		mntmRunSelectedTest.setAccelerator(SWT.F8);
		mntmRunSelectedTest.setText("Run selected test" + "\tF8");
		
		MenuItem mntmOptions = new MenuItem(menu, SWT.CASCADE);
		mntmOptions.setText("&Options");
		
		Menu menu_3 = new Menu(mntmOptions);
		mntmOptions.setMenu(menu_3);
		
		MenuItem mntmAutoSaveConfiguration = new MenuItem(menu_3, SWT.CHECK);
		mntmAutoSaveConfiguration.setSelection(true);
		mntmAutoSaveConfiguration.setText("&Auto Save Configuration");
		
		MenuItem mntmErrorBoxVisible = new MenuItem(menu_3, SWT.CHECK);
		mntmErrorBoxVisible.setSelection(true);
		mntmErrorBoxVisible.setText("&Error Box Visible");
		
		MenuItem mntmAutoChangeFocus = new MenuItem(menu_3, SWT.CHECK);
		mntmAutoChangeFocus.setSelection(true);
		mntmAutoChangeFocus.setText("Auto Change &Focus");
		
		MenuItem mntmHideTestNodesOnOpen = new MenuItem(menu_3, SWT.CHECK);
		mntmHideTestNodesOnOpen.setText("&Hide Test Nodes On Open");
		
		MenuItem mntmShowTestedNode = new MenuItem(menu_3, SWT.CHECK);
		mntmShowTestedNode.setSelection(true);
		mntmShowTestedNode.setText("&Show Tested Node");
		
		MenuItem mntmBreakOnFailures = new MenuItem(menu_3, SWT.CHECK);
		mntmBreakOnFailures.setText("&Break on Failures");
		
		MenuItem mntmUseRegistry = new MenuItem(menu_3, SWT.CHECK);
		mntmUseRegistry.setText("Use Registry");
		
		new MenuItem(menu_3, SWT.SEPARATOR);

		MenuItem mntmShowTestCasesWithRunTimeProperties = new MenuItem(menu_3, SWT.CHECK);
		mntmShowTestCasesWithRunTimeProperties.setText("Show TestCases with RunTime Properties");
		
		MenuItem mntmWarnIfFailTestOverridden = new MenuItem(menu_3, SWT.CHECK);
		mntmWarnIfFailTestOverridden.setText("Warn if Fail Test Overridden");
		
		new MenuItem(menu_3, SWT.SEPARATOR);

		MenuItem mntmFailTestCaseIfNoChecksExecuted = new MenuItem(menu_3, SWT.CHECK);
		mntmFailTestCaseIfNoChecksExecuted.setText("Fail TestCase if no checks executed");
		
		new MenuItem(menu_3, SWT.SEPARATOR);

		MenuItem mntmReportMemoryLeakTypeOnShutdown = new MenuItem(menu_3, SWT.CHECK);
		mntmReportMemoryLeakTypeOnShutdown.setText("Report memory leak type on Shutdown");
		
		MenuItem mntmFailTestCaseIfMemoryLeaked = new MenuItem(menu_3, SWT.CHECK);
		mntmFailTestCaseIfMemoryLeaked.setText("Fail TestCase if memory leaked");
		
		MenuItem mntmIgnoreMemoryLeakInSetUpTearDown = new MenuItem(menu_3, SWT.CHECK);
		mntmIgnoreMemoryLeakInSetUpTearDown.setText("Ignore memory leak in SetUp/TearDown");
		
		MenuItem mntmActions = new MenuItem(menu, SWT.CASCADE);
		mntmActions.setText("Actio&ns");
		
		Menu menu_4 = new Menu(mntmActions);
		mntmActions.setMenu(menu_4);
		
		MenuItem mntmRun = new MenuItem(menu_4, SWT.NONE);
		mntmRun.setAccelerator(SWT.F9);
		mntmRun.setText("Run" + "\tF9");
		
		MenuItem mntmRunSelectedTest2 = new MenuItem(menu_4, SWT.NONE);
		mntmRunSelectedTest2.setAccelerator(SWT.F8);
		mntmRunSelectedTest2.setText("Run selected test" + "\tF8");
		
		MenuItem mntmStop = new MenuItem(menu_4, SWT.NONE);
		mntmStop.setAccelerator(SWT.CTRL | SWT.F2);
		mntmStop.setText("&Stop" + "\tCtrl+F2");
		
		MenuItem mntmCopyErrorMessageToClipboard = new MenuItem(menu_4, SWT.NONE);
		mntmCopyErrorMessageToClipboard.setEnabled(false);
		mntmCopyErrorMessageToClipboard.setAccelerator(SWT.SHIFT | SWT.CTRL | 'C');
		mntmCopyErrorMessageToClipboard.setText("&Copy Error Message to Clipboard" + "\tShift+Ctrl+C");
		
		toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.bottom = new FormAttachment(0, 24);
		fd_toolBar.right = new FormAttachment(100, -4);
		fd_toolBar.top = new FormAttachment(0);
		fd_toolBar.left = new FormAttachment(0, 4);
		toolBar.setLayoutData(fd_toolBar);
		
		SashForm sashForm = new SashForm(shell, SWT.VERTICAL);
		FormData fd_sashForm = new FormData();
		fd_sashForm.top = new FormAttachment(toolBar, 4);
		
		ToolItem tltmSelectall = new ToolItem(toolBar, SWT.NONE);
		tltmSelectall.setText("Select &All");
		
		ToolItem tltmDeselectall = new ToolItem(toolBar, SWT.NONE);
		tltmDeselectall.setText("&Deselect All");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmSelectfailed = new ToolItem(toolBar, SWT.NONE);
		tltmSelectfailed.setText("Select Fai&led");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmSelectcurrent = new ToolItem(toolBar, SWT.NONE);
		tltmSelectcurrent.setText("Select &Current");
		
		ToolItem tltmDeselectcurrent = new ToolItem(toolBar, SWT.NONE);
		tltmDeselectcurrent.setText("Deselect C&urrent");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmRun = new ToolItem(toolBar, SWT.NONE);
		tltmRun.setText("&Run");
		
		ToolItem tltmRunselectedtest = new ToolItem(toolBar, SWT.NONE);
		tltmRunselectedtest.setText("Run selected test");
		
		ToolItem tltmStop = new ToolItem(toolBar, SWT.NONE);
		tltmStop.setText("&Stop");
		fd_sashForm.right = new FormAttachment(100, -4);
		fd_sashForm.bottom = new FormAttachment(100, -4);
		fd_sashForm.left = new FormAttachment(0, 4);
		sashForm.setLayoutData(fd_sashForm);
		
		Composite compositeTree = new Composite(sashForm, SWT.NONE);
		compositeTree.setLayout(new FormLayout());
		
		Label lblTestHierarchy = new Label(compositeTree, SWT.NONE);
		FormData fd_lblTestHierarchy = new FormData();
		fd_lblTestHierarchy.right = new FormAttachment(100);
		fd_lblTestHierarchy.top = new FormAttachment(0);
		fd_lblTestHierarchy.left = new FormAttachment(0);
		lblTestHierarchy.setLayoutData(fd_lblTestHierarchy);
		lblTestHierarchy.setText("Test Hi&erarchy:");
		
		testTree = new Tree(compositeTree, SWT.BORDER | SWT.CHECK);
		FormData fd_testTree = new FormData();
		fd_testTree.top = new FormAttachment(lblTestHierarchy, 6);
		fd_testTree.left = new FormAttachment(0);
		fd_testTree.bottom = new FormAttachment(100, -3);
		fd_testTree.right = new FormAttachment(100);
		testTree.setLayoutData(fd_testTree);
		
		Composite compositeResults = new Composite(sashForm, SWT.NONE);
		compositeResults.setLayout(new FormLayout());
		
		Composite compositeProgressAndScore = new Composite(compositeResults, SWT.BORDER);
		compositeProgressAndScore.setLayout(new FormLayout());
		FormData fd_compositeProgressAndScore = new FormData();
		fd_compositeProgressAndScore.bottom = new FormAttachment(0, 56);
		fd_compositeProgressAndScore.right = new FormAttachment(100);
		fd_compositeProgressAndScore.top = new FormAttachment(0, 3);
		fd_compositeProgressAndScore.left = new FormAttachment(0);
		compositeProgressAndScore.setLayoutData(fd_compositeProgressAndScore);
		
		Label lblProgress = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblProgress = new FormData();
		fd_lblProgress.right = new FormAttachment(0, 59);
		fd_lblProgress.top = new FormAttachment(0, 6);
		fd_lblProgress.left = new FormAttachment(0, 10);
		lblProgress.setLayoutData(fd_lblProgress);
		lblProgress.setText("Progress:");
		
		ProgressBar progressBar = new ProgressBar(compositeProgressAndScore, SWT.NONE);
		FormData fd_progressBar = new FormData();
		fd_progressBar.height = 12;
		fd_progressBar.top = new FormAttachment(0, 6);
		fd_progressBar.left = new FormAttachment(lblProgress, 6);
		fd_progressBar.right = new FormAttachment(100, -6);
		progressBar.setLayoutData(fd_progressBar);
		
		Label lblScore = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblScore = new FormData();
		fd_lblScore.right = new FormAttachment(lblProgress, 0, SWT.RIGHT);
		fd_lblScore.top = new FormAttachment(lblProgress, 6);
		fd_lblScore.left = new FormAttachment(lblProgress, 0, SWT.LEFT);
		lblScore.setLayoutData(fd_lblScore);
		lblScore.setText("Score:");
		
		ProgressBar scoreBar = new ProgressBar(compositeProgressAndScore, SWT.NONE);
		FormData fd_scoreBar = new FormData();
		fd_scoreBar.right = new FormAttachment(100, -56);
		fd_scoreBar.height = 12;
		fd_scoreBar.top = new FormAttachment(progressBar, 6);
		fd_scoreBar.left = new FormAttachment(progressBar, 0, SWT.LEFT);
		scoreBar.setLayoutData(fd_scoreBar);
		
		Label lblProgressPercent = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblProgressPercent = new FormData();
		fd_lblProgressPercent.top = new FormAttachment(lblScore, 0, SWT.TOP);
		fd_lblProgressPercent.left = new FormAttachment(scoreBar, 6);
		fd_lblProgressPercent.right = new FormAttachment(100, -6);
		lblProgressPercent.setLayoutData(fd_lblProgressPercent);
		lblProgressPercent.setText("Progress");
		
		tableResults = new Table(compositeResults, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_tableResults = new FormData();
		fd_tableResults.height = 16;
		fd_tableResults.top = new FormAttachment(compositeProgressAndScore, 3);
		fd_tableResults.right = new FormAttachment(100, 0);
		fd_tableResults.left = new FormAttachment(0, 0);
		tableResults.setLayoutData(fd_tableResults);
		tableResults.setHeaderVisible(true);
		
		tableFailureList = new Table(compositeResults, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_tableFailureList = new FormData();
		fd_tableFailureList.bottom = new FormAttachment(100, -3);
		fd_tableFailureList.left = new FormAttachment(0, 0);
		fd_tableFailureList.right = new FormAttachment(100, 0);
		fd_tableFailureList.top = new FormAttachment(tableResults, 3);
		
		TableColumn tableColumn = new TableColumn(tableResults, SWT.NONE);
		tableColumn.setWidth(8);
		
		TableColumn tblclmnTests = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTests.setWidth(70);
		tblclmnTests.setText("Tests");
		
		TableColumn tblclmnRun = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnRun.setWidth(70);
		tblclmnRun.setText("Run");
		
		TableColumn tblclmnFailures = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnFailures.setWidth(70);
		tblclmnFailures.setText("Failures");
		
		TableColumn tblclmnErrors = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnErrors.setWidth(70);
		tblclmnErrors.setText("Errors");
		
		TableColumn tblclmnOverrides = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnOverrides.setWidth(70);
		tblclmnOverrides.setText("Overrides");
		
		TableColumn tblclmnTestTime = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTestTime.setWidth(52);
		tblclmnTestTime.setText("Test Time");
		
		TableColumn tblclmnTotalTime = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTotalTime.setWidth(70);
		tblclmnTotalTime.setText("Total Time");
		
		TableItem tableItem = new TableItem(tableResults, SWT.NONE);
		tableItem.setText(new String[] {"", "1", "2", "3", "4", "5", "6", "7"});
		tableFailureList.setLayoutData(fd_tableFailureList);
		tableFailureList.setHeaderVisible(true);
		
		TableColumn tblclmnTestName = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnTestName.setWidth(120);
		tblclmnTestName.setText("Test Name");
		
		TableColumn tblclmnFailureType = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnFailureType.setWidth(100);
		tblclmnFailureType.setText("Failure Type");
		
		TableColumn tblclmnMessage = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnMessage.setWidth(200);
		tblclmnMessage.setText("Message");
		
		TableColumn tblclmnLocation = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnLocation.setWidth(60);
		tblclmnLocation.setText("Location");
		
		Composite compositeErrorBox = new Composite(sashForm, SWT.NONE);
		compositeErrorBox.setLayout(new FormLayout());
		
		StyledText styledText = new StyledText(compositeErrorBox, SWT.BORDER);
		FormData fd_styledText = new FormData();
		fd_styledText.bottom = new FormAttachment(100);
		fd_styledText.right = new FormAttachment(100);
		fd_styledText.top = new FormAttachment(0, 3);
		fd_styledText.left = new FormAttachment(0);
		styledText.setLayoutData(fd_styledText);
		sashForm.setWeights(new int[] {200, 150, 50});

	}

	private void fillTestSuite() {

		Request request = Request.aClass(testClass);
		Runner runner = request.getRunner();
		Description rootDescription = runner.getDescription();
		TreeItem rootItem = new TreeItem(testTree, 0);
		rootItem.setText(rootDescription.getDisplayName());
		rootItem.setData(rootDescription);
		Deque<TreeItem> stack = new ArrayDeque<>();
		stack.push(rootItem);
		while (stack.size() > 0) {
			TreeItem curItem = stack.pop();
			Description curDescription = (Description) curItem.getData();
			if (curDescription.isSuite()) {
				//TestSuite testSuite = (TestSuite) curTest;
				for (Description childDescripton: curDescription.getChildren()) {
					TreeItem item = new TreeItem(curItem, 0);
					item.setText(childDescripton.toString());
					item.setData(childDescripton);
					stack.push(item);
				}
			}
		}
	}
}
