package ru.vitkud.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.wb.swt.SWTResourceManager;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

public class TestRunner extends RunListener {

	// Color constants for the progress bar and failure details panel
	private Color CL_OK;// = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	private Color CL_FAILURE;// = Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA);
	private Color CL_ERROR;// = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	// color images used in the test tree and failure list
	@SuppressWarnings("unused") // TODO ...
	private Image imgNone;
	private Image imgRunning;
	private Image imgRun;
	@SuppressWarnings("unused") // TODO ...
	private Image imgHasProps;
	private Image imgFailed;
	private Image imgError;

	private String suiteName;
	private Class<?> suite;

	private ArrayList<Description> fTests;
	private Map<Description, TreeItem> testToNodeMap;
	private boolean fRunning;
	private Result result;
	//private Timer fUpdateTimer;
	//private boolean fTimerExpired;
	private int fFailureCount;
	private List<Description> fSelectedTests;

	@SuppressWarnings("unused") // TODO ...
	private long fTotalTime;

//	private static Image[] actionsImages;

	protected Display display;
	protected Shell shell;
	private ToolBar toolBar;
	private Table tableResults;
	private Table tableFailureList;
	private Tree testTree;
	private MenuItem mntmFailTestCaseIfMemoryLeaked;
	private MenuItem mntmReportMemoryLeakTypeOnShutdown;
	private MenuItem mntmIgnoreMemoryLeakInSetUpTearDown;
	private MenuItem mntmHideTestNodesOnOpen;
	private ProgressBar scoreBar;
	private MenuItem mntmShowTestedNode;

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
			System.exit(window.getfFailureCount());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TestRunner(String suiteName) throws ClassNotFoundException {
		this.suiteName = suiteName;
		this.suite = Class.forName(suiteName);

//		loadImages();
	}

//	private void loadImages() {
//	{
//		actionsImages = new Image[11];
//		for (int i = 0; i < actionsImages.length; ++i) {
//			actionsImages[i] = new Image(Display.getDefault(), getClass().getResourceAsStream("images/actions/" + i + ".png"));
//		}
//	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();

		initColors();
		loadImages();
		
		formCreate();
		setSuite();
		formShow();

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void loadImages() {
		imgNone = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png");
		imgRunning = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/1.png");
		imgRun = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/2.png");
		imgHasProps = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/3.png");
		imgFailed = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/4.png");
		imgError = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/5.png");
	}

	private void initColors() {
		CL_OK = display.getSystemColor(SWT.COLOR_GREEN);
		CL_FAILURE = display.getSystemColor(SWT.COLOR_MAGENTA);
		CL_ERROR = display.getSystemColor(SWT.COLOR_RED);
	}

	private void setSuite() {
		if (suite != null) {
			loadSuiteConfiguration();
			enableUI(true);
			initTree();
		} else {
			enableUI(false);
		}
		
	}

	private void initTree() {
		fillTestTree();
		setup();
		if (mntmHideTestNodesOnOpen.getSelection()) {
			// TODO HideTestNodesAction.Execute
		} else {
			// TODO ExpandAllNodesAction.Execute;
		}
		//TODO TestTree.Selected := TestTree.Items.GetFirstNode;
	}

	private void fillTestTree() {
		testTree.removeAll();
		fTests.clear();
		testToNodeMap.clear();

		Request request = Request.aClass(suite);
		Runner runner = request.getRunner();
		Description rootDescription = runner.getDescription();

		TreeItem rootItem = new TreeItem(testTree, SWT.NONE);

		fillTestTree(rootItem, rootDescription);
	}

	private void fillTestTree(TreeItem treeItem, Description description) {
		if (description == null)
			return;

		treeItem.setText(description.getDisplayName());
		treeItem.setData(fTests.size());
		treeItem.setChecked(true); // TODO ...
		treeItem.setImage(SWTResourceManager.getImage(TestRunner.class, "images/run/0.png"));

		fTests.add(description);
		testToNodeMap.put(description, treeItem);

		for (Description childDescripton: description.getChildren()) {
			fillTestTree(new TreeItem(treeItem, SWT.NONE), childDescripton);
		}
	}

	private void loadSuiteConfiguration() {
		// TODO Auto-generated method stub
		
	}

	private void formCreate() {
//		fillTestSuite();
		fTests = new ArrayList<>();
		testToNodeMap = new HashMap<>();
		loadConfiguration();

		// TODO setupCustomShortcuts;
		testTree.removeAll(); // XXX
		enableUI(false);
		clearFailureMessage();
		setup();

		mntmFailTestCaseIfMemoryLeaked.setEnabled(false);
		mntmFailTestCaseIfMemoryLeaked.setEnabled(false);
		mntmReportMemoryLeakTypeOnShutdown.setEnabled(false);
		
		// TODO if not FailTestCaseIfMemoryLeakedAction.Enabled then
		// TODO   FailTestCaseIfMemoryLeakedAction.Checked := False;
		// TODO IgnoreMemoryLeakInSetUpTearDownAction.Enabled :=
		// TODO   FailTestCaseIfMemoryLeakedAction.Checked;
		// TODO if not IgnoreMemoryLeakInSetUpTearDownAction.Enabled then
		// TODO   IgnoreMemoryLeakInSetUpTearDownAction.Checked := False;
	}

	private void formShow() {
		setupGuiNodes();
	}

	private void setupGuiNodes() {
		// TODO Auto-generated method stub
		
	}

	private void setup() {
		// TODO Auto-generated method stub
		
	}

	private void clearFailureMessage() {
		// TODO Auto-generated method stub
		
	}

	private void enableUI(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	private void loadConfiguration() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setMinimumSize(new Point(300, 200));
		shell.setSize(500, 500);
		shell.setText("JUnit GUI - " + suiteName);
		FormLayout fl_shell = new FormLayout();
		fl_shell.marginBottom = 4;
		fl_shell.marginRight = 4;
		fl_shell.marginLeft = 4;
		shell.setLayout(fl_shell);

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
		mntmExit.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/6.png"));
		mntmExit.setAccelerator(SWT.ALT | 'X');
		mntmExit.setText("E&xit" + "\tAlt+X");
		
		MenuItem mntmTestTree = new MenuItem(menu, SWT.CASCADE);
		mntmTestTree.setText("Test &Tree");
		
		Menu menu_2 = new Menu(mntmTestTree);
		mntmTestTree.setMenu(menu_2);
		
		MenuItem mntmSelectAll = new MenuItem(menu_2, SWT.NONE);
		mntmSelectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/0.png"));
		mntmSelectAll.setAccelerator(SWT.CTRL | SWT.ALT | 'A');
		mntmSelectAll.setText("Select &All" + "\tCtrl+Alt+A");
		
		MenuItem mntmDeselectAll = new MenuItem(menu_2, SWT.NONE);
		mntmDeselectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/1.png"));
		mntmDeselectAll.setAccelerator(SWT.CTRL | SWT.DEL);
		mntmDeselectAll.setText("&Deselect All" + "\tCtrl+Del");
		
		MenuItem mntmSelectFailed = new MenuItem(menu_2, SWT.NONE);
		mntmSelectFailed.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/4.png"));
		mntmSelectFailed.setAccelerator(SWT.CTRL | 'F');
		mntmSelectFailed.setText("Select Fai&led" + "\tCtrl+F");
		
		MenuItem mntmSelectCurrent = new MenuItem(menu_2, SWT.NONE);
		mntmSelectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/2.png"));
		mntmSelectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | 'A');
		mntmSelectCurrent.setText("Select &Current" + "\tShift+Ctrl+A");
		
		MenuItem mntmDeselectCurrent = new MenuItem(menu_2, SWT.NONE);
		mntmDeselectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/3.png"));
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
		mntmRunSelectedTest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
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
		
		mntmHideTestNodesOnOpen = new MenuItem(menu_3, SWT.CHECK);
		mntmHideTestNodesOnOpen.setText("&Hide Test Nodes On Open");
		
		mntmShowTestedNode = new MenuItem(menu_3, SWT.CHECK);
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

		mntmReportMemoryLeakTypeOnShutdown = new MenuItem(menu_3, SWT.CHECK);
		mntmReportMemoryLeakTypeOnShutdown.setText("Report memory leak type on Shutdown");
		
		mntmFailTestCaseIfMemoryLeaked = new MenuItem(menu_3, SWT.CHECK);
		mntmFailTestCaseIfMemoryLeaked.setText("Fail TestCase if memory leaked");
		
		mntmIgnoreMemoryLeakInSetUpTearDown = new MenuItem(menu_3, SWT.CHECK);
		mntmIgnoreMemoryLeakInSetUpTearDown.setText("Ignore memory leak in SetUp/TearDown");
		
		MenuItem mntmActions = new MenuItem(menu, SWT.CASCADE);
		mntmActions.setText("Actio&ns");
		
		Menu menu_4 = new Menu(mntmActions);
		mntmActions.setMenu(menu_4);
		
		MenuItem mntmRun = new MenuItem(menu_4, SWT.NONE);
		mntmRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				run();
			}
		});
		mntmRun.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/7.png"));
		mntmRun.setAccelerator(SWT.F9);
		mntmRun.setText("Run" + "\tF9");
		
		MenuItem mntmRunSelectedTest2 = new MenuItem(menu_4, SWT.NONE);
		mntmRunSelectedTest2.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
		mntmRunSelectedTest2.setAccelerator(SWT.F8);
		mntmRunSelectedTest2.setText("Run selected test" + "\tF8");
		
		MenuItem mntmStop = new MenuItem(menu_4, SWT.NONE);
		mntmStop.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/8.png"));
		mntmStop.setAccelerator(SWT.CTRL | SWT.F2);
		mntmStop.setText("&Stop" + "\tCtrl+F2");
		
		MenuItem mntmCopyErrorMessageToClipboard = new MenuItem(menu_4, SWT.NONE);
		mntmCopyErrorMessageToClipboard.setEnabled(false);
		mntmCopyErrorMessageToClipboard.setAccelerator(SWT.SHIFT | SWT.CTRL | 'C');
		mntmCopyErrorMessageToClipboard.setText("&Copy Error Message to Clipboard" + "\tShift+Ctrl+C");
		
		Label menuSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_menuSeparator = new FormData();
		fd_menuSeparator.right = new FormAttachment(100);
		fd_menuSeparator.left = new FormAttachment(0);
		menuSeparator.setLayoutData(fd_menuSeparator);
		
		toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.top = new FormAttachment(menuSeparator);
		fd_toolBar.right = new FormAttachment(100);
		fd_toolBar.left = new FormAttachment(0);
		toolBar.setLayoutData(fd_toolBar);
		
		ToolItem tltmSelectall = new ToolItem(toolBar, SWT.NONE);
		tltmSelectall.setToolTipText("Select all tests");
		tltmSelectall.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/0.png"));
		
		ToolItem tltmDeselectall = new ToolItem(toolBar, SWT.NONE);
		tltmDeselectall.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/1.png"));
		tltmDeselectall.setToolTipText("Deselect all tests");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmSelectfailed = new ToolItem(toolBar, SWT.NONE);
		tltmSelectfailed.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/4.png"));
		tltmSelectfailed.setToolTipText("Select all failed tests");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmSelectcurrent = new ToolItem(toolBar, SWT.NONE);
		tltmSelectcurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/2.png"));
		tltmSelectcurrent.setToolTipText("Select current test");
		
		ToolItem tltmDeselectcurrent = new ToolItem(toolBar, SWT.NONE);
		tltmDeselectcurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/3.png"));
		tltmDeselectcurrent.setToolTipText("Deselect current test");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmRun = new ToolItem(toolBar, SWT.NONE);
		tltmRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				run();
			}
		});
		tltmRun.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/7.png"));
		tltmRun.setToolTipText("Run selected tests");
		
		ToolItem tltmRunselectedtest = new ToolItem(toolBar, SWT.NONE);
		tltmRunselectedtest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
		tltmRunselectedtest.setToolTipText("Run current test");
		
		ToolItem tltmStop = new ToolItem(toolBar, SWT.NONE);
		tltmStop.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/8.png"));
		tltmStop.setToolTipText("Stop");

		Label toolBarSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_toolBarSeparator = new FormData();
		fd_toolBarSeparator.top = new FormAttachment(toolBar);
		fd_toolBarSeparator.right = new FormAttachment(100);
		fd_toolBarSeparator.left = new FormAttachment(0);
		toolBarSeparator.setLayoutData(fd_toolBarSeparator);

		SashForm sashForm = new SashForm(shell, SWT.VERTICAL);
		FormData fd_sashForm = new FormData();
		fd_sashForm.top = new FormAttachment(toolBarSeparator, 4);
		fd_sashForm.right = new FormAttachment(100);
		fd_sashForm.bottom = new FormAttachment(100);
		fd_sashForm.left = new FormAttachment(0);
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
		testTree.setToolTipText("Hierarchy of test cases. Checked test cases will be run.");
		FormData fd_testTree = new FormData();
		fd_testTree.top = new FormAttachment(lblTestHierarchy, 4);
		fd_testTree.left = new FormAttachment(0);
		fd_testTree.bottom = new FormAttachment(100, -3);
		fd_testTree.right = new FormAttachment(100);
		testTree.setLayoutData(fd_testTree);
		
		TreeItem trtmTest = new TreeItem(testTree, SWT.NONE);
		trtmTest.setChecked(true);
		trtmTest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmTest.setText("Test1");
		
		TreeItem trtmSubtest = new TreeItem(trtmTest, SWT.NONE);
		trtmSubtest.setChecked(true);
		trtmSubtest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmSubtest.setText("SubTest");
		
		TreeItem trtmSubtest_1 = new TreeItem(trtmTest, SWT.NONE);
		trtmSubtest_1.setChecked(true);
		trtmSubtest_1.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmSubtest_1.setText("SubTest1.2");
		trtmTest.setExpanded(true);
		
		TreeItem trtmTest_1 = new TreeItem(testTree, SWT.NONE);
		trtmTest_1.setChecked(true);
		trtmTest_1.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmTest_1.setText("Test2");
		
		Composite compositeResults = new Composite(sashForm, SWT.NONE);
		compositeResults.setLayout(new FormLayout());
		
		Composite compositeProgressAndScore = new Composite(compositeResults, SWT.BORDER);
		compositeProgressAndScore.setToolTipText("");
		compositeProgressAndScore.setLayout(new FormLayout());
		FormData fd_compositeProgressAndScore = new FormData();
		fd_compositeProgressAndScore.bottom = new FormAttachment(0, 52);
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
		progressBar.setToolTipText("Shows the proportion of tests run");
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
		
		scoreBar = new ProgressBar(compositeProgressAndScore, SWT.BORDER);
		scoreBar.setToolTipText("Shows the proportion of successful tests");
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
		tableResults.setToolTipText("Shows statistics about the current/last run");
		FormData fd_tableResults = new FormData();
		fd_tableResults.height = 16;
		fd_tableResults.top = new FormAttachment(compositeProgressAndScore, 3);
		fd_tableResults.right = new FormAttachment(100, 0);
		fd_tableResults.left = new FormAttachment(0, 0);
		tableResults.setLayoutData(fd_tableResults);
		tableResults.setHeaderVisible(true);
		
		tableFailureList = new Table(compositeResults, SWT.BORDER | SWT.FULL_SELECTION);
		tableFailureList.setToolTipText("Shows the list of failed tests");
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
		styledText.setText("ErrorMessageRTF");
		FormData fd_styledText = new FormData();
		fd_styledText.bottom = new FormAttachment(100);
		fd_styledText.right = new FormAttachment(100);
		fd_styledText.top = new FormAttachment(0, 3);
		fd_styledText.left = new FormAttachment(0);
		styledText.setLayoutData(fd_styledText);
		sashForm.setWeights(new int[] {200, 150, 50});

	}

	protected void run() {
		if (suite == null)
			return;

		setup();
		runTheTest();
	}

	private void runTheTest() {
		if (suite == null)
			return;

		if (fRunning) {
			// warning: we're reentering this method if fRunning is true
			// TODO assert(fTestResult != null);
			// TODO fTestResult.stop();
			return;
		}

		fRunning = true;
		try {

			Request request = Request.aClass(suite).filterWith(new RunTheTestFilter());
			Runner runner = request.getRunner();

			// TODO RunAction.Enabled  := False;
			// TODO StopAction.Enabled := True;

			// TODO CopyMessageToClipboardAction.Enabled := false;

			enableUI(false);
			// TODO autoSaveConfiguration();
			// TODO clearResult();

			final RunNotifier notifier = new RunNotifier();
			notifier.addListener(new RunTheTestListener());
			result = new Result();
			RunListener listener = result.createListener();
			notifier.addListener(listener);
			try {
				// TODO TestResult.BreakOnFailures := BreakOnFailuresAction.Checked;
				// TODO TestResult.FailsIfNoChecksExecuted := FailIfNoChecksExecutedAction.Checked;
				// TODO TestResult.FailsIfMemoryLeaked := FailTestCaseIfMemoryLeakedAction.Checked;
				// TODO TestResult.IgnoresMemoryLeakInSetUpTearDown := IgnoreMemoryLeakInSetUpTearDownAction.Checked;
				notifier.fireTestRunStarted(runner.getDescription());
				runner.run(notifier);
				notifier.fireTestRunFinished(result);
			} catch (StoppedByUserException e) {
				// not interesting
			} finally {
				// TODO ? FErrorCount := TestResult.ErrorCount;
				//fIgnoreCount = result.getIgnoreCount();
				fFailureCount = result.getFailureCount();
				// TODO ? TestResult.Free;
				// TODO ?TestResult := nil;
			}
		} finally {
			fRunning = false;
			enableUI(true);
		}
	}

	private TreeItem testToNode(Description test) {
		return testToNodeMap.get(test);
	}

	public class RunTheTestFilter extends Filter {

		@Override
		public boolean shouldRun(Description description) {
			if (fSelectedTests == null) {
				TreeItem node = testToNode(description);
				return node != null && node.getChecked();
			} else {
				return fSelectedTests.contains(description);
			}
		}

		@Override
		public String describe() {
			return getClass().getSimpleName();
		}

	}

	private class RunTheTestListener extends RunListener {

		boolean lastTestFail;

		// This may be called on an arbitrary thread.
		@Override
		public void testRunStarted(final Description description) throws Exception {
	    	if (display.isDisposed()) return;
	    	display.syncExec(new Runnable() {
				@Override public void run() {
			    	if (display.isDisposed()) return;
					testingStarts(description);
				}
			});
		}

		// This may be called on an arbitrary thread.
		@Override
		public void testRunFinished(final Result result) throws Exception {
	    	if (!display.isDisposed()) {
	    		display.syncExec(new Runnable() {
	    			@Override public void run() {
	    				if (!display.isDisposed())
	    					testingEnds(result);
	    			}
	    		});
	    	}
		}

		public void testStarted(final Description description) throws Exception {
			lastTestFail = false;
	    	if (!display.isDisposed()) {
	    		display.syncExec(new Runnable() {
	    			@Override public void run() {
	    				if (!display.isDisposed())
	    					startTest(description);
	    			}
	    		});
	    	}
		}

		public void testFinished(final Description description) throws Exception {
	    	if (!display.isDisposed()) {
	    		display.syncExec(new Runnable() {
	    			@Override public void run() {
	    				if (!display.isDisposed()) {
	    					if (!lastTestFail)
	    						addSuccess(description);
	    					endTest(description);
	    				}
	    			}
	    		});
	    	}
		}

		//  may be called on an arbitrary thread.
		public void testFailure(final Failure failure) throws Exception {
			lastTestFail = true;
			if (!display.isDisposed()) {
				display.syncExec(new Runnable() {
					@Override public void run() {
						if (!display.isDisposed()) {
							if (failure.getException() instanceof AssertionError)
								addFailure(failure);
							else 
								addError(failure);
						}
					}
				});
			}
		}

		public void testAssumptionFailure(final Failure failure) {
			lastTestFail = true;
			if (!display.isDisposed()) {
				display.syncExec(new Runnable() {
					@Override public void run() {
						if (!display.isDisposed()) {
							addFailure(failure); // TODO ...
						}
					}
				});
			}
		}

		public void testIgnored(Description description) throws Exception {
			// TODO ..
		}

	}

	protected void testingStarts(Description description) {
    	fTotalTime = 0;
    	updateStatus(true);
    	scoreBar.setBackground(CL_OK);
    	scoreBar.update();
	}

	protected void testingEnds(Result result) {
		fTotalTime = result.getRunTime();
	}

	protected void startTest(Description description) {
    	assert result != null;
    	assert description != null;
    	TreeItem node = testToNode(description);
    	assert node != null;
    	setTreeNodeImage(node, imgRunning);
		if (mntmShowTestedNode.getSelection()) {
			makeNodeVisible(node);
			testTree.update();
		}
		clearStatusMessage();
		updateStatus(false);
	}

	protected void endTest(Description description) {
		updateStatus(false);
	}

	protected void addSuccess(Description description) {
		assert description != null;
		setTreeNodeImage(testToNode(description), imgRun);
	}

	protected void addError(Failure failure) {
		TableItem item = addFailureItem(failure);
		item.setImage(imgError);
		scoreBar.setBackground(CL_ERROR);
		scoreBar.update();

		setTreeNodeImage(testToNode(failure.getDescription()), imgError);
		updateStatus(false);
	}

	protected void addFailure(Failure failure) {
		TableItem item = addFailureItem(failure);
		item.setImage(imgFailed);
		if (!CL_ERROR.equals(scoreBar.getBackground())) {
			scoreBar.setBackground(CL_FAILURE);
			scoreBar.update();
		}
		setTreeNodeImage(testToNode(failure.getDescription()), imgFailed);
		updateStatus(false);
	}

	private TableItem addFailureItem(Failure failure) {
		// TODO Auto-generated method stub
		return null;
	}

	private void updateStatus(boolean b) {
		// TODO Auto-generated method stub
		
	}

	private void setTreeNodeImage(TreeItem node, Image imgRunning2) {
		// TODO Auto-generated method stub
		
	}

	private void makeNodeVisible(TreeItem node) {
		// TODO Auto-generated method stub
		
	}

	private void clearStatusMessage() {
		// TODO Auto-generated method stub
		
	}

//	private void fillTestSuite() {
//		Request request = Request.aClass(testClass);
//		Runner runner = request.getRunner();
//		Description rootDescription = runner.getDescription();
//		TreeItem rootItem = new TreeItem(testTree, 0);
//		rootItem.setText(rootDescription.getDisplayName());
//		rootItem.setData(rootDescription);
//		Deque<TreeItem> stack = new ArrayDeque<>();
//		stack.push(rootItem);
//		while (stack.size() > 0) {
//			TreeItem curItem = stack.pop();
//			Description curDescription = (Description) curItem.getData();
//			if (curDescription.isSuite()) {
//				//TestSuite testSuite = (TestSuite) curTest;
//				for (Description childDescripton: curDescription.getChildren()) {
//					TreeItem item = new TreeItem(curItem, 0);
//					item.setText(childDescripton.toString());
//					item.setData(childDescripton);
//					stack.push(item);
//				}
//			}
//		}
//	}

	public int getfFailureCount() {
		return fFailureCount;
	}

}
