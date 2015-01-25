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

@interface CGLoginViewController () <FHSTwitterEngineAccessTokenDelegate>

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

@end

@implementation CGLoginViewController

- (void)viewDidAppear:(BOOL)animated
{
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [[FHSTwitterEngine sharedEngine] setDelegate:self];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)authButtonClick:(id)sender
{
    [self performSegueWithIdentifier:@"signUpSegue" sender:sender];
}

#pragma mark -
#pragma mark Sign In

- (IBAction)signInViaFB:(id)sender
{
    [self showHUD];
    
    [self.loginner signInUsingFbWithBlock:^(NSError *error)
    {
        if (!error)
        {
            NSLog(@"SuccesLogin");
            
            AppDelegate *appDelegateTemp = [[UIApplication sharedApplication] delegate];
            appDelegateTemp.window.rootViewController = [self.storyboard instantiateInitialViewController];
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
            
            AppDelegate *appDelegateTemp = [[UIApplication sharedApplication] delegate];
            appDelegateTemp.window.rootViewController = [self.storyboard instantiateInitialViewController];
        }
        else
        {
            [self handleError:error];
            
            NSLog(@"%@", [error description]);
        }
        [self hideHUD];
    }];
}

- (void)storeAccessToken:(NSString *)accessToken
{
    [[NSUserDefaults standardUserDefaults] setObject:accessToken forKey:@"SavedAccessHTTPBody"];
}

- (NSString *)loadAccessToken
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:@"SavedAccessHTTPBody"];
}

@end
