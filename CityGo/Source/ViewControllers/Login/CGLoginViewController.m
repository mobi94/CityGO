//
//  CGLoginViewController.m
//  CityGo
//
//  Created by ruslan on 1/22/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGLoginViewController.h"
#import "CGViewControllerProtected.h"
#import "CGSignInProtocol.h"
#import "AppDelegate.h"
#import "FHSTwitterEngine.h"
#import <STAlertView/STAlertView.h>
#import "ICETutorialController.h"

static NSArray  *SCOPE = nil;

@interface CGLoginViewController () <FHSTwitterEngineAccessTokenDelegate, VKSdkDelegate, UIAlertViewDelegate, UITextFieldDelegate, ICETutorialControllerDelegate>

@property(weak, nonatomic) IBOutlet id<CGSignInProtocol> loginner;

@property (weak, nonatomic) IBOutlet UIImageView *logoImageVIew;
@property (weak, nonatomic) IBOutlet UITextField *usernameTextField;
@property (weak, nonatomic) IBOutlet UITextField *passwordTextField;
@property (weak, nonatomic) IBOutlet UIButton *authButton;
@property (weak, nonatomic) IBOutlet UIButton *fogotPasswordButton;
@property (weak, nonatomic) IBOutlet UIButton *facebookButton;
@property (weak, nonatomic) IBOutlet UIButton *twitterButton;
@property (weak, nonatomic) IBOutlet UIButton *vkButton;
@property (weak, nonatomic) IBOutlet UILabel *privacyPolicyLabel;

@property (strong, nonatomic) ICETutorialController *tutorialViewController;

@end

@implementation CGLoginViewController

- (void)viewDidAppear:(BOOL)animated
{
    [self.authButton setTitle:@"Sign Up" forState:UIControlStateNormal];
    
    if (![STANDART_USER_DEFAULTS boolForKey:@"secondLaunch"])
    {
        [self setupTutorialController];
        [STANDART_USER_DEFAULTS setBool:YES forKey:@"secondLaunch"];
        [STANDART_USER_DEFAULTS synchronize];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [[FHSTwitterEngine sharedEngine] setDelegate:self];
    
    [VKSdk initializeWithDelegate:self andAppId:@"4749201"];
    SCOPE = @[VK_PER_WALL, VK_PER_PHOTOS, VK_PER_NOHTTPS];
    
    [self setupTextFields];
    [self setupButton:self.authButton];
    [self setupButton:self.facebookButton];
    [self setupButton:self.twitterButton];
    [self setupButton:self.vkButton];
    
    [self.fogotPasswordButton setTitleColor:CG_WHITE_COLOR forState:UIControlStateNormal];
    
    [self.privacyPolicyLabel setTextColor:CG_WHITE_COLOR];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)authButtonClick:(id)sender
{
    if ([self userInfoReady])
    {
        [self signInViaUserInfo];
        return;
    }
    
    [self performSegueWithIdentifier:@"signUpSegue" sender:sender];
}

#pragma mark -
#pragma mark Custom Setup

- (void)setupTextFields
{
    [self.usernameTextField addTarget:self action:@selector(textFieldDidChange:) forControlEvents:UIControlEventEditingChanged];
    [self.passwordTextField addTarget:self action:@selector(textFieldDidChange:) forControlEvents:UIControlEventEditingChanged];
}

- (void)setupButton:(UIButton *)button
{
    [[button layer] setCornerRadius:4];
    [[button layer] setBorderColor:CG_WHITE_COLOR.CGColor];
    [[button layer] setBorderWidth:0.5];
    [button setBackgroundColor:[UIColor clearColor]];
    [button setTitleColor:CG_WHITE_COLOR forState:UIControlStateNormal];
    [button setClipsToBounds:YES];
}

- (void)setInitialViewController
{
    AppDelegate *appDelegateTemp = [[UIApplication sharedApplication] delegate];
    appDelegateTemp.window.rootViewController = [self.storyboard instantiateInitialViewController];
}

- (void)setupTutorialController
{
    // Init the pages texts, and pictures.
    ICETutorialPage *layer1 = [[ICETutorialPage alloc] initWithTitle:@"Picture 1"
                                                            subTitle:@"Champs-Elysées by night"
                                                         pictureName:@"tutorial_background_00@2x.jpg"
                                                            duration:3.0];
    ICETutorialPage *layer2 = [[ICETutorialPage alloc] initWithTitle:@"Picture 2"
                                                            subTitle:@"The Eiffel Tower with\n cloudy weather"
                                                         pictureName:@"tutorial_background_01@2x.jpg"
                                                            duration:3.0];
    ICETutorialPage *layer3 = [[ICETutorialPage alloc] initWithTitle:@"Picture 3"
                                                            subTitle:@"An other famous street of Paris"
                                                         pictureName:@"tutorial_background_02@2x.jpg"
                                                            duration:3.0];
    ICETutorialPage *layer4 = [[ICETutorialPage alloc] initWithTitle:@"Picture 4"
                                                            subTitle:@"The Eiffel Tower with a better weather"
                                                         pictureName:@"tutorial_background_03@2x.jpg"
                                                            duration:3.0];
    ICETutorialPage *layer5 = [[ICETutorialPage alloc] initWithTitle:@"Picture 5"
                                                            subTitle:@"The Louvre's Museum Pyramide"
                                                         pictureName:@"tutorial_background_04@2x.jpg"
                                                            duration:3.0];
    NSArray *tutorialLayers = @[layer1,layer2,layer3,layer4,layer5];
    
    // Set the common style for the title.
    ICETutorialLabelStyle *titleStyle = [[ICETutorialLabelStyle alloc] init];
    [titleStyle setFont:[UIFont fontWithName:@"Helvetica-Bold" size:17.0f]];
    [titleStyle setTextColor:[UIColor whiteColor]];
    [titleStyle setLinesNumber:1];
    [titleStyle setOffset:180];
    [[ICETutorialStyle sharedInstance] setTitleStyle:titleStyle];
    
    // Set the subTitles style with few properties and let the others by default.
    [[ICETutorialStyle sharedInstance] setSubTitleColor:[UIColor whiteColor]];
    [[ICETutorialStyle sharedInstance] setSubTitleOffset:150];
    
    // Init tutorial.
    self.tutorialViewController = [[ICETutorialController alloc] initWithPages:tutorialLayers
                                                                      delegate:self];
    
    [self presentViewController:self.tutorialViewController animated:YES completion:nil];
}

#pragma mark -
#pragma mark Actions

- (BOOL)userInfoReady
{
    return (self.usernameTextField.text.length > 0 && self.passwordTextField.text.length > 0);
}

- (void)textFieldDidChange:(UITextField*)textField
{
    
    if ([self userInfoReady])
    {
        [self.authButton setTitle:@"Sign In" forState:UIControlStateNormal];
    }
    else
    {
        [self.authButton setTitle:@"Sign Up" forState:UIControlStateNormal];
    }
}

#pragma mark -
#pragma mark Sign In

- (void)signInViaUserInfo
{
    [self showHUD];
    
    [self.loginner signInUsingUserInfo:@{@"username" : self.usernameTextField.text, @"password" : self.passwordTextField.text} WithBlock:^(NSError *error)
    {
        if (!error)
        {
            NSLog(@"SuccesLogin");
            
            [self setInitialViewController];
        }
        else
        {
            [self handleError:error];
            
            NSLog(@"%@", [error description]);
        }
        [self hideHUD];
    }];
}

- (IBAction)signInViaFB:(id)sender
{
    [self showHUD];
    
    [self.loginner signInUsingFbWithBlock:^(NSError *error)
    {
        if (!error)
        {
            NSLog(@"SuccesLogin");
            
            [self setInitialViewController];
        }
        else
        {
            [self handleError:error];
            
            NSLog(@"%@", [error description]);
        }
        [self hideHUD];
    }];
}

- (IBAction)signInViaTwitter:(id)sender
{
    [self showHUD];
    
    [self.loginner signInUsingTwitter:self WithBlock:^(NSError *error)
    {
        if (!error)
        {
            NSLog(@"SuccesLogin");
            
            [self setInitialViewController];
        }
        else
        {
            [self handleError:error];
            
            NSLog(@"%@", [error description]);
        }
        [self hideHUD];
    }];
}

- (IBAction)signInViaVk:(id)sender
{
    [VKSdk authorize:SCOPE revokeAccess:YES forceOAuth:YES];
}

- (void)signInViaVk
{
    [self showHUD];
    
    [self.loginner signInUsingVkWithBlock:^(NSError *error)
    {
        if (!error)
        {
            NSLog(@"SuccesLogin");
            
            [self setInitialViewController];
        }
        else
        {
            [self handleError:error];
            
            NSLog(@"%@", [error description]);
        }
        [self hideHUD];
    }];
}

- (IBAction)fogotPassword:(id)sender
{
    UIAlertView *recoveryPassword = [[UIAlertView alloc] initWithTitle:@"Restore Password" message:@"PLease, enter your account email" delegate:self cancelButtonTitle:@"Send" otherButtonTitles:@"Cancel", nil];
    [recoveryPassword setAlertViewStyle:UIAlertViewStylePlainTextInput];
    
    [recoveryPassword show];
}

#pragma mark -
#pragma mark - Alert View Delegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0)
    {
        UITextField *emailField = [alertView textFieldAtIndex:0];
        [PFUser requestPasswordResetForEmailInBackground:emailField.text];
    }
    else
    {
        NSLog(@"cancel");
    }
}

#pragma mark -
#pragma mark Twitter Engine Delegates

- (void)storeAccessToken:(NSString *)accessToken
{
    [[NSUserDefaults standardUserDefaults] setObject:accessToken forKey:@"SavedAccessHTTPBody"];
}

- (NSString *)loadAccessToken
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:@"SavedAccessHTTPBody"];
}

#pragma mark -
#pragma mark VK Delegates

- (void)vkSdkNeedCaptchaEnter:(VKError *)captchaError
{
    VKCaptchaViewController *vc = [VKCaptchaViewController captchaControllerWithError:captchaError];
    [vc presentIn:self];
}

- (void)vkSdkTokenHasExpired:(VKAccessToken *)expiredToken
{
    [VKSdk authorize:SCOPE revokeAccess:NO];
}

- (void)vkSdkReceivedNewToken:(VKAccessToken *)newToken
{
    [self signInViaVk];
}

- (void)vkSdkShouldPresentViewController:(UIViewController *)controller
{
    [self presentViewController:controller animated:YES completion:nil];
}

- (void)vkSdkAcceptedUserToken:(VKAccessToken *)token
{
    [self signInViaVk];
}

- (void)vkSdkUserDeniedAccess:(VKError *)authorizationError
{
    NSLog(@"Access denied");
}

#pragma mark - ICETutorialController delegate
- (void)tutorialController:(ICETutorialController *)tutorialController scrollingFromPageIndex:(NSUInteger)fromIndex toPageIndex:(NSUInteger)toIndex
{
    NSLog(@"Scrolling from page %lu to page %lu.", (unsigned long)fromIndex, (unsigned long)toIndex);
}

- (void)tutorialControllerDidReachLastPage:(ICETutorialController *)tutorialController
{
    NSLog(@"Tutorial reached the last page.");
}

- (void)tutorialController:(ICETutorialController *)tutorialController didClickOnLeftButton:(UIButton *)sender
{
    [self.tutorialViewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)tutorialController:(ICETutorialController *)tutorialController didClickOnRightButton:(UIButton *)sender
{
    NSLog(@"Button 2 pressed.");
}

@end
